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
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;
import utils.VerboseLevel;

public class JointActionExchangingAgent extends FullMCTSGhostAgent {
    private final Map<GHOST, MoveMessage> received_moves = new EnumMap<GHOST, MoveMessage>(GHOST.class);
    private int moves_message_interval;
    private long last_message_sending_time = 0;
    private long total_simulations = 0;


    public JointActionExchangingAgent(DistributedMCTSController controller, final GHOST ghost, int simulation_depth, double ucb_coef, int moves_message_interval, final VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
        hookMoveMessageHandler(received_moves);
        this.moves_message_interval = moves_message_interval;
    }

    public JointActionExchangingAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, int moves_message_interval) {
        this(controller, ghost, simulation_depth, ucb_coef, moves_message_interval, VerboseLevel.QUIET);
    }

    private void sendMessages() {
        long current_time = controller.currentMillis();

        if (current_time-last_message_sending_time>moves_message_interval) {
            broadcastMoveMessage(Priority.HIGH);
            last_message_sending_time = current_time;
        }
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            total_simulations++;
        }
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        return getMoveFromMessages(received_moves);
    }

    @Override
    public long totalSimulations() {
        return total_simulations;
    }

}
