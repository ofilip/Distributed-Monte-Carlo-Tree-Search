package mcts.distributed.agents;

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
    protected Game current_game;
    protected int current_level;
    protected EnumMap<GHOST, MOVE> last_full_move;

    public FullMCTSGhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
    }

    private void initializeTree(Game game) {
        mctree = new GhostsTree(game, ucb_selector, my_simulator, backpropagator, ucb_coef);
    }

    @Override public MCTree getTree() { return mctree; }

    @Override public void updateTree(Game game) {        
        if (mctree==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=current_level /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||last_full_move==null /* last getMove() didn't finish in limit */
                ||!Utils.compareGhostsMoves(last_full_move, Utils.lastGhostsMoves(game))
                ) {
            /* (re)initialize MC-tree and its components */
            initializeTree(game);

            /* remember current level */
            current_level = game.getCurrentLevel();
        } else {
            assert current_game!=null;
            EnumMap<Constants.GHOST, MOVE> last_ghosts_moves = Utils.lastGhostsDecisionMoves(game, current_game);

            if (mctree.root().ticksToGo()==0) {
                initializeTree(game);
            } else {
                mctree.advanceTree(game.getPacmanLastMoveMade(), last_ghosts_moves);
            }
        }

        current_game = game.copy();
        last_full_move = null;
    }

    private Comparator<Map.Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>> move_strength_entry_comparator = new Comparator<Map.Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>>() {
        @Override
        public int compare(Map.Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t1, Map.Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t2) {
            int i1 = t1.getValue().first;
            int i2 = t2.getValue().first;

            if (i1<i1) {
                return -1;
            } else if (i1>i2) {
                return 1;
            } else {
                int g1 = t1.getValue().second.ordinal();
                int g2 = t2.getValue().second.ordinal();

                if (g1<g2) {
                    return -1;
                } else if (g1>g2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    };

    protected void hookMoveMessageHandler(final Map<GHOST, MoveMessage> received_moves) {
        hookMessageHandler(MoveMessage.class, new MessageHandler() {
            @Override
            public void handleMessage(GhostAgent agent, Message message) {
                MoveMessage moves_message = (MoveMessage)message;
                if (verbose.check(VerboseLevel.DEBUGGING)) {
                    System.out.printf("%s: Receiving %s\n", ghost, moves_message);
                }
                received_moves.put(agent.ghost(), moves_message);
            }
        });
    }

    protected void broadcastMoveMessage(Priority priority) {
            EnumMap<GHOST, MOVE> best_move = mctree.bestMove(current_game);
            MoveMessage message = new MoveMessage(best_move);
            if (verbose.check(VerboseLevel.DEBUGGING)) {
                System.out.printf("%s: Broadcasting %s (size %s)\n", ghost, message, message.length());
            }
            broadcastMessage(Priority.MEDIUM, message);
    }

    protected MOVE getMoveFromMessages(final Map<GHOST, MoveMessage> received_moves) {
                /* Return strongest move (move supported by the most agents)
         * with priority defined by ordering on GHOST enum. */
        EnumMap<GHOST, MOVE> my_best_move = mctree.bestMove(current_game);
        received_moves.put(ghost, new MoveMessage(my_best_move)); /* add my current best move to received messages */

        Map<EnumMap<GHOST,MOVE>, Pair<Integer, GHOST>> move_strength = new HashMap<EnumMap<GHOST,MOVE>, Pair<Integer, GHOST>>();

        for (Map.Entry<GHOST, MoveMessage> entry: received_moves.entrySet()) {
            GHOST g = entry.getKey();
            MoveMessage message = entry.getValue();
            Pair<Integer, GHOST> current_value = move_strength.get(message.moves());
            if (current_value==null) {
                move_strength.put(message.moves(), new Pair<Integer, GHOST>(1, g));
            } else {
                current_value.first++;
            }
        }
        last_full_move = Collections.min(move_strength.entrySet(), move_strength_entry_comparator).getKey();

        if (verbose.check(VerboseLevel.VERBOSE)) {
            System.out.printf(ghost+": ");
            System.out.printf("my best move: %s\n", my_best_move);
            System.out.printf("\tchosen best move: %s\n", last_full_move);
        }
        return last_full_move.get(ghost);
    }
}
