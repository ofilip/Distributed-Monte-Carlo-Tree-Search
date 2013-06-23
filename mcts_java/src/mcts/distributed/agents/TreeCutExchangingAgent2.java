package mcts.distributed.agents;

import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import communication.messages.TreeCutMessage;
import communication.messages.TreeNodeMessage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

public class TreeCutExchangingAgent2 extends FullMCTSGhostAgent {
    private long calculatedSimulations = 0;
    private long maxBytesSize = 1024;
    private long receivedSimulations = 0;
    private Map<GhostAgent, TreeCutMessage> last_message_received = new HashMap<GhostAgent, TreeCutMessage>();

    private long removeReceivedSimulations(GhostAgent agent) {
        TreeCutMessage previous_message = last_message_received.get(agent);
        long removed = 0;

        if (previous_message!=null) {
            for (TreeNodeMessage msg: previous_message.nodeMessages()) {
                long suppressed = 0;
                try {
                    suppressed = mctree.applyTreeNode(agent.ghost(), msg.treeMoves(), 0, 0);
                } catch (InvalidActionListException ex) {
                    assert(false);
                }
                assert(msg.count()==suppressed);
                removed += msg.count();
            }
        }

        return removed;
    }

    private void removeAllReceivedSimulations() {
        for (GhostAgent agent: last_message_received.keySet()) {
            removeReceivedSimulations(agent);
        }
    }

    @Override
    protected void postTreeInit() {
        last_message_received.clear();
    }

    @Override
    protected void preTreeAdvancing(boolean willAdvance) {
        if (willAdvance) {
            removeAllReceivedSimulations();
            last_message_received.clear();
        }
    }

    public TreeCutExchangingAgent2(final DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);

        hookMessageHandler(TreeCutMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                TreeCutMessage cut_message = (TreeCutMessage)message;
                if (ghost==GHOST.BLINKY) {
                    //System.err.printf("Tree cut received (%s,%d,%d)\n", agent.ghost, cut_message.length(), receivedSimulations);
                }
                try {
                    /* remove previous cut */
                    long removed = removeReceivedSimulations(agent);
                    receivedSimulations -= removed;

                    /* apply received cut */

                    for (TreeNodeMessage result_message: cut_message.nodeMessages()) {
                        int root_visit_count_before = mctree.root().visitCount();
                        long maskedSimulations = mctree.applyTreeNode(agent.ghost(), result_message.treeMoves(), result_message.simulationResult(), result_message.count());
                        receivedSimulations += result_message.count() - maskedSimulations;
                        assert(maskedSimulations==0);
                        assert(mctree.root().visitCount()==root_visit_count_before+result_message.count());
                    }
                } catch (InvalidActionListException e) {
                    assert(false);
                }
                last_message_received.put(agent, cut_message);
            }
        });
    }

    private void sendMessages() {
        for (GhostAgent ally: messageSenders.keySet()) {
            MessageSender sender = messageSenders.get(ally);
            double secondsPerSimulation = 1/controller.simulationsPerSecond();
            if (sender.sendQueueLength()==0||sender.secondsToSendAll() <= secondsPerSimulation*3) {
                TreeCutMessage msg = new TreeCutMessage(VisitCountTreeCut.createRootCut(mctree, maxBytesSize, 30, true));
                sender.send(Priority.HIGHEST, msg);
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
            sendMessages();
        }
    }

    @Override
    public MOVE getMove() {
        if (ghost==GHOST.BLINKY) {
            //System.err.printf("==MOVE==\n");
        }
        if (verboseLevel.check(VerboseLevel.DEBUGGING)) {
            System.err.printf("[%s] %s\n", ghost, mctree);
        }
        lastFullMove = mctree.bestMove(currentGame);
        return lastFullMove.get(ghost);
    }

    @Override public long calculatedSimulations() { return calculatedSimulations; }
    public long receivedSimulations() { return receivedSimulations; }


    @Override
    public long totalSimulations() {
        return calculatedSimulations+receivedSimulations();
    }

    //TODO: messages transmitted, average length of message, received simulations...


    public void setCutByteSize(long bytes) {
        maxBytesSize = bytes;
    }

    public long getCutByteSize() {
        return maxBytesSize;
    }
}
