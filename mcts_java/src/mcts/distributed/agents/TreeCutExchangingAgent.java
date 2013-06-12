package mcts.distributed.agents;

import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import communication.messages.TreeNodeMessage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.MCNode;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.TreeCut;
import mcts.distributed.TreeCutIterator;
import mcts.distributed.TreeCutNode;
import mcts.distributed.VisitCountTreeCut;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class TreeCutExchangingAgent extends FullMCTSGhostAgent {
    private long calculatedSimulations = 0;
    private long totalSimulatonResultsMessageLength = 0;
    private long simulationResultsMessagesCount = 0;
    private TreeCut treeCut;
    private Map<GHOST, TreeCutIterator> cutIterators = new EnumMap<GHOST, TreeCutIterator>(GHOST.class);
    private int visitCountThreshold = 30;
    private long maxBytesSize = 1024;
    private long receivedSimulations = 0;
    private long summedCutSize = 0;
    private double cutsTransmitted = 0;

    private void initTreeCut() {
        treeCut = VisitCountTreeCut.createRootCut(mctree, maxBytesSize, visitCountThreshold);
        for (GHOST ally: GHOST.values()) {
            if (ally==ghost) continue;
            TreeCutIterator it = cutIterators.get(ally);
            if (it!=null) {
                cutsTransmitted += it.iterationCount();
            }
            cutIterators.put(ally, treeCut.registerIterator());
        }
    }

    @Override
    protected void postTreeInit() {
        initTreeCut();
    }

    @Override
    protected void postTreeAdvancing(int steps) {
        if (steps>0) {
            initTreeCut();
        }
    }

    public TreeCutExchangingAgent(final DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);

        hookMessageHandler(TreeNodeMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                TreeNodeMessage result_message = (TreeNodeMessage)message;
                try {
                    String tree_str_before = null;
                    int root_visit_count_before = mctree.root().visitCount();
                    if (verboseLevel.check(VerboseLevel.DEBUGGING)&&ghost==GHOST.BLINKY) {
                        tree_str_before = mctree.toString();
                    }
                    long maskedSimulations = mctree.applyTreeNode(agent.ghost(), result_message.treeMoves(), result_message.simulationResult(), result_message.count());
                    receivedSimulations += result_message.count() - maskedSimulations;
                    assert(mctree.root().visitCount()==root_visit_count_before+result_message.count()-maskedSimulations);
                    if (verboseLevel.check(VerboseLevel.DEBUGGING)&&ghost==GHOST.BLINKY) {
                        String tree_str_after = mctree.toString();
                        System.err.printf("[RECEIVING] %s%s [from %s] .. +%s -%s moves=%s\n%s", tree_str_before, receivedSimulations, agent.ghost, result_message.count(), maskedSimulations, result_message.treeMoves(), tree_str_after);
                    }
                } catch (InvalidActionListException e) { assert(false); }
            }
        });
    }

    private void sendMessages() {
        for (GhostAgent ally: messageSenders.keySet()) {
            TreeCutIterator it = cutIterators.get(ally.ghost);
            MessageSender sender = messageSenders.get(ally);
            TreeCutNode previous = it.current();
            while (sender.sendQueueItemsCount()<2) {
                TreeNodeMessage msg = it.next().toMessage();
                if (msg.count()==0) {
                    continue;
                }
                sender.send(Priority.MEDIUM, msg);
                if (previous==it.current()) break; /* don't send same message */
            }
        }
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            if (verboseLevel.check(VerboseLevel.DEBUGGING)&&ghost==GHOST.BLINKY) {
                System.err.printf("[ITERATION] %s", mctree.toString());
            }
            calculatedSimulations++;
            treeCut.reexpand();
            sendMessages();
        }
    }

    @Override
    public MOVE getMove() {
        if (verboseLevel.check(VerboseLevel.DEBUGGING)) {
            System.err.printf("[%s] %s\n", ghost, mctree);
        }
        lastFullMove = mctree.bestMove(currentGame);
        summedCutSize += treeCut.bytesSize();
        return lastFullMove.get(ghost);
    }

    @Override public long calculatedSimulations() { return calculatedSimulations; }
    public long receivedSimulations() { return receivedSimulations; }


    @Override
    public long totalSimulations() {
        return calculatedSimulations+receivedSimulations();
    }

    //TODO: messages transmitted, average length of message, received simulations...

    public double averageSimulatonResultsMessageLength() {
        return totalSimulatonResultsMessageLength/(double)Math.max(1, simulationResultsMessagesCount);
    }

    public void setCutByteSize(long bytes) {
        maxBytesSize = bytes;
    }

    public long getCutByteSize() {
        return maxBytesSize;
    }

    public double averageCutSize() {
        return summedCutSize/(double)currentGame.getTotalTime();
    }

    public double cutsTransmitted() { return cutsTransmitted; }
}
