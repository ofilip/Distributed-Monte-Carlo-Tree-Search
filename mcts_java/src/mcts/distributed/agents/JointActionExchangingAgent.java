package mcts.distributed.agents;

import communication.Priority;
import communication.messages.Message;
import communication.messages.MoveMessage;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcts.Constants;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;
import utils.VerboseLevel;

public class JointActionExchangingAgent extends FullMCTSGhostAgent implements AgentSendingRegularMessages {
    private final Map<GHOST, MoveMessage> received_moves = new EnumMap<GHOST, MoveMessage>(GHOST.class);
    private long moveMessageInterval = Constants.DEFAULT_MESSAGE_INTERVAL;
    private long lastMessageSendTime = 0;
    private long totalSimulations = 0;


    public JointActionExchangingAgent(DistributedMCTSController controller, final GHOST ghost, int moves_message_interval) {
        super(controller, ghost);
        hookMoveMessageHandler(received_moves);
        this.moveMessageInterval = moves_message_interval;
    }

    private void sendMessages() {
        long currentTime = controller.currentMillis();

        if (currentTime-lastMessageSendTime>moveMessageInterval) {
            broadcastMoveMessage(Priority.HIGH);
            lastMessageSendTime = currentTime;
        }
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            totalSimulations++;
        }
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        return getMoveFromMessages(received_moves);
    }

    @Override
    public long totalSimulations() {
        return totalSimulations;
    }

    public long getMessageInterval() {
        return moveMessageInterval;
    }

    public void setMessageInterval(long interval) {
        this.moveMessageInterval = interval;
    }

}
