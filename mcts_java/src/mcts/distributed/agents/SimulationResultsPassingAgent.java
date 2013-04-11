package mcts.distributed.agents;

import communication.Priority;
import communication.messages.Message;
import communication.messages.MoveMessage;
import communication.messages.SimulationResultMessage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mcts.Action;
import mcts.distributed.DistributedMCTSController;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class SimulationResultsPassingAgent extends FullMCTSGhostAgent {
    private int MOVE_MESSAGE_INTERVAL = 50;
    private long simulations_sent = 0;
    private final Map<GHOST, MoveMessage> received_moves = new EnumMap<GHOST, MoveMessage>(GHOST.class);
    private long total_simulations = 0;

    public SimulationResultsPassingAgent(DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);

        hookMoveMessageHandler(received_moves);

        hookMessageHandler(SimulationResultMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                SimulationResultMessage result_message = (SimulationResultMessage)message;
                try {
                    mctree.applySimulationResult(result_message.treeMoves(), result_message.simulationResult());
                } catch (InvalidActionListException e) { assert(false); }
            }
        });
    }

    private void sendMessages(List<Action> action_list, double simulation_result) {
        //TODO:
        // * aggregated information passing
        // * load ballancing
        // * push message at the beginning of message queue

        /* Broadcast simulation results and enqueue messages before prevously enqueued simulation messages */
        SimulationResultMessage message = new SimulationResultMessage(action_list, simulation_result);
        broadcastMessage(Priority.MEDIUM, message, true);

        /* Each MOVE_MESSAGE_INTERVAL simulation messages add a move message */
        if (simulations_sent++%MOVE_MESSAGE_INTERVAL==0) {
            broadcastMoveMessage(Priority.HIGH);
        }
    }

    @Override
    public void step() {
        List<Action> action_list = new ArrayList<Action>();

        receiveMessages();
        double simulation_result = mctree.iterate(action_list);
        if (!Double.isNaN(simulation_result)) {
            total_simulations++;
            sendMessages(action_list, simulation_result);
        }
    }

    @Override
    public MOVE getMove() {
        simulations_sent = 0;
        return getMoveFromMessages(received_moves);
    }

    @Override
    public long totalSimulations() {
        return total_simulations;
    }

}
