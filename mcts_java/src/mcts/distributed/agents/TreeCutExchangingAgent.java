package mcts.distributed.agents;

import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import communication.messages.TreeNodeMessage;
import java.util.HashMap;
import java.util.Map;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.TreeCut;
import mcts.distributed.TreeCutIterator;
import mcts.distributed.VisitCountTreeCut;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class TreeCutExchangingAgent extends FullMCTSGhostAgent {
    private long calculatedSimulations = 0;
    private long totalSimulatonResultsMessageLength = 0;
    private long simulationResultsMessagesCount = 0;
    private TreeCut treeCut;
    private Map<GHOST, TreeCutIterator> cutIterators = new HashMap<GHOST, TreeCutIterator>();
    private int visitCountThreshold = 10;
    private long maxBytesSize = 1024;

    @Override
    protected void postTreeInit() {
        treeCut = VisitCountTreeCut.createRootCut(mctree, maxBytesSize, visitCountThreshold);
        for (GHOST ally: GHOST.values()) {
            cutIterators.put(ally, treeCut.registerIterator());
        }
    }

    public TreeCutExchangingAgent(final DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);

        hookMessageHandler(TreeNodeMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                TreeNodeMessage result_message = (TreeNodeMessage)message;
                try {
                    mctree.applyTreeNode(result_message.treeMoves(), result_message.simulationResult(), result_message.count());
                } catch (InvalidActionListException e) { assert(false); }
            }
        });
    }

    private void sendMessages() {
        for (GhostAgent ally: messageSenders.keySet()) {
            TreeCutIterator it = cutIterators.get(ally.ghost);
            MessageSender sender = messageSenders.get(ally);

            while (sender.sendQueueItemsCount()<2) {
                sender.send(Priority.MEDIUM, it.next().toMessage());
            }
        }
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            calculatedSimulations++;
            treeCut.reexpand();
            sendMessages();
        }
    }

    @Override
    public MOVE getMove() {
        lastFullMove = mctree.bestMove(currentGame);
        return lastFullMove.get(ghost);
    }

    @Override public long calculatedSimulations() { return calculatedSimulations; }
    public long receivedSimulations() { return 0; } //TODO: go through the tree and sum received_visit_count values

    @Override
    public long totalSimulations() {
        return calculatedSimulations+receivedSimulations();
    }

    //TODO: messages transmitted, average length of message, received simulations...

    public double averageSimulatonResultsMessageLength() {
        return totalSimulatonResultsMessageLength/(double)Math.max(1, simulationResultsMessagesCount);
    }

    public void setCutByteSize(long bytes) {
        treeCut.setMaxBytesSize(bytes);
    }
}
