package mcts.distributed.agents;

import communication.MessageCallback;
import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import communication.messages.MoveMessage;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.GhostsTree;
import mcts.MCTree;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.Pair;
import utils.VerboseLevel;

public abstract class FullMCTSGhostAgent extends GhostAgent {
    protected MOVE move = MOVE.NEUTRAL;
    protected GhostsTree mctree;
    protected int currentLevel;
    protected EnumMap<GHOST, MOVE> lastFullMove;
    protected EnumMap<GHOST, MOVE> lastTransmittedMove = Utils.NEUTRAL_GHOSTS_MOVES; //XXX
    protected long decisions = 0;
    protected boolean optimisticTurns = true;

    public FullMCTSGhostAgent(DistributedMCTSController controller, GHOST ghost) {
        super(controller, ghost);
    }

    private void initializeTree(Game game) {
        mctree = new GhostsTree(game, ucbSelector, mySimulator, backpropagator, ucbCoef);
        mctree.setOptimisticTurns(optimisticTurns);
    }

    @Override public MCTree getTree() { return mctree; }

    @Override public void updateTree(Game game) {

        if (mctree==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=currentLevel /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||lastFullMove==null /* last getMove() didn't finish in limit */
  //              ||!Utils.ghostMovesEqual(lastFullMove, Utils.lastGhostsMoves(game))
                ||mctree.root().getTotalTicks()>mySimulator.getMaxDepth()/2 /* simulation is too much shortened */
                ) {
            /* (re)initialize MC-tree and its components */
            initializeTree(game);

            /* remember current level */
            currentLevel = game.getCurrentLevel();

            /* restore random seed */
            if (equalRandomSeed) {
                random.setSeed(randomSeed);
            }
        } else {
            assert currentGame!=null;
            EnumMap<Constants.GHOST, MOVE> lastGhostsMoves = Utils.lastGhostsDecisionMoves(game, currentGame);

            if (mctree.root().ticksToGo()==0) {
                initializeTree(game);
            } else {
                mctree.advanceTree(game.getPacmanLastMoveMade(), lastGhostsMoves);
            }
        }

        currentGame = game.copy();
        lastFullMove = null;
        if (mctree.decisionNeeded()) {
            decisions++;
        }
    }

    private final static Comparator<Map.Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>> moveStrengthEntryComparator = new Comparator<Map.Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>>() {
        @Override
        public int compare(Map.Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t1, Map.Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t2) {
            int i1 = t1.getValue().first;
            int i2 = t2.getValue().first;

            if (i1<i2) {
                return -1;
            } else if (i1>i2) {
                return 1;
            } else {
                int g1 = t1.getValue().second.ordinal();
                int g2 = t2.getValue().second.ordinal();

                if (g1<g2) { /* less (BLINKY) is better */
                    return 1;
                } else if (g1>g2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    };

    protected void hookMoveMessageHandler(final Map<GHOST, MoveMessage> receivedMoves) {
        hookMessageHandler(MoveMessage.class, new MessageHandler() {
            @Override
            public void handleMessage(GhostAgent agent, Message message) {
                MoveMessage moveMessage = (MoveMessage)message;

                if (verboseLevel.check(VerboseLevel.DEBUGGING)) {
                    System.out.printf("[%d] %s from %s: Receiving %s\n", controller.currentVirtualMillis(), ghost, agent.ghost(), moveMessage);
                }
                receivedMoves.put(agent.ghost(), moveMessage);
            }
        });
    }

    protected void broadcastMoveMessage(Priority priority, EnumMap<GHOST, MOVE> bestMove) {
        MoveMessage message = new MoveMessage(bestMove);
        //if (true) {
        if (verboseLevel.check(VerboseLevel.DEBUGGING)) {
            System.out.printf("[%s] %s: Broadcasting %s (size %s)\n", controller.currentVirtualMillis(), ghost, message, message.length());
        }

        lastTransmittedMove = bestMove;

        flushMessages(MoveMessage.class);
        broadcastMessage(Priority.MEDIUM, message, true);
    }

    protected MOVE nextMoveFromMessages(Game currentGame, final Map<GHOST, MoveMessage> receivedMoves/*, final EnumMap<GHOST,MOVE> myMove*/) {
        if (mctree.decisionNeeded()) {
            return getMoveFromMessages(receivedMoves/*, myMove*/);
        } else {
            return Utils.ghostsFollowRoads(currentGame).get(ghost);
        }
    }

    protected MOVE getMoveFromMessages(final Map<GHOST, MoveMessage> receivedMoves/*, final EnumMap<GHOST,MOVE> myMove*/) {
        /* Return strongest move (move supported by the most agents)
         * with priority defined by ordering on GHOST enum. */
        //EnumMap<GHOST, MOVE> myBestMove = mctree.bestDecisionMove(); XXX
        receiveMessages();
        receivedMoves.put(ghost, new MoveMessage(/*XXX myBestMove*//*myMove*/lastTransmittedMove)); /* add my current best move to received messages */

        Map<EnumMap<GHOST,MOVE>, Pair<Integer, GHOST>> moveStrength = new HashMap<EnumMap<GHOST,MOVE>, Pair<Integer, GHOST>>();

        for (Map.Entry<GHOST, MoveMessage> entry: receivedMoves.entrySet()) {
            GHOST g = entry.getKey();
            MoveMessage message = entry.getValue();
            Pair<Integer, GHOST> currentValue = moveStrength.get(message.moves());
            if (currentValue==null) {
                moveStrength.put(message.moves(), new Pair<Integer, GHOST>(1, g));
            } else {
                currentValue.first++;
            }
        }
        lastFullMove = Collections.max(moveStrength.entrySet(), moveStrengthEntryComparator).getKey();

        if (verboseLevel.check(VerboseLevel.VERBOSE)&&Utils.ghostsNeedAction(currentGame)) {
            System.out.printf("["+controller.currentVirtualMillis()+"]"+ghost+": ");
            System.out.printf("my best move: %s\n", /*myMove*/lastTransmittedMove/*XXX myBestMove*/);
            System.out.printf("\tchosen best move: %s\n", lastFullMove);
            for (EnumMap<GHOST,MOVE> ghostMove: moveStrength.keySet()) {
                Pair<Integer, GHOST> valuePair = moveStrength.get(ghostMove);
                System.out.printf("\tvariant: %s (%s,%s)\n", ghostMove, valuePair.first, valuePair.second);
            }
        }

        return lastFullMove.get(ghost);
    }

    @Override
    public EnumMap<GHOST, MOVE> getFullMove() {
        return lastFullMove;
    }

    public long currentTreeSize() { return mctree.root().visitCount(); }

    public double averageDecisionSimulations() {
        return totalSimulations()/(double)decisions;
    }

    @Override public boolean getOptimisticTurns() { return optimisticTurns; }
    @Override public void setOptimisticTurns(boolean optimisticTurns) { this.optimisticTurns = optimisticTurns; }
}
