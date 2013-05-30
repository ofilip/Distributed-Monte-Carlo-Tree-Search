package mcts.distributed.agents;

import communication.MessageCallback;
import communication.Priority;
import communication.messages.Message;
import communication.messages.MoveMessage;
import communication.messages.SimulationResultMessage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mcts.Action;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class SimulationResultsPassingAgent extends FullMCTSGhostAgent {
    private long receivedSimulations = 0;
    private long calculatedSimulations = 0;
    private long totalSimulatonResultsMessageLength = 0;
    private long simulationResultsMessagesCount = 0;
    private long stepsSinceLastMoveSent = 0;

    public SimulationResultsPassingAgent(final DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);

        hookMessageHandler(SimulationResultMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                SimulationResultMessage result_message = (SimulationResultMessage)message;
                try {
//                    System.out.printf("[%s=>%s,%s] Receiving simulation: %s\n", agent.ghost, ghost, controller.currentVirtualMillis(), message);
                    receivedSimulations++;
                    mctree.applySimulationResult(result_message.treeMoves(), result_message.simulationResult());
                } catch (InvalidActionListException e) { assert(false); }
            }
        });
    }

    private void sendMessages(List<Action> actionList, double simulationResults) {
        //TODO:
        // * aggregated information passing
        // * load ballancing
        // * push message at the beginning of message queue

        /* Broadcast simulation results and enqueue messages before prevously enqueued simulation messages */
        SimulationResultMessage message = new SimulationResultMessage(actionList, simulationResults);
        totalSimulatonResultsMessageLength += message.length();
        simulationResultsMessagesCount++;
        broadcastMessage(Priority.MEDIUM, message, true);
    }

    @Override
    public void step() {
        stepsSinceLastMoveSent++;
        receiveMessages();
        List<Action> action_list = new ArrayList<Action>();
        double simulation_result = mctree.iterate(action_list);
        if (!Double.isNaN(simulation_result)) {
            calculatedSimulations++;
            sendMessages(action_list, simulation_result);
        }
    }

    @Override
    public MOVE getMove() {
        //MOVE result = getMoveFromMessages(receivedMoves/*, mctree.bestDecisionMove()*/);
        lastFullMove = mctree.bestMove(currentGame);
        //return result;
        return lastFullMove.get(ghost);
    }

    @Override public long calculatedSimulations() { return calculatedSimulations; }
    public long receivedSimulations() { return receivedSimulations; }

    @Override
    public long totalSimulations() {
        return calculatedSimulations+receivedSimulations;
    }

    public long getSimulationResultsMessageCount() {
        return simulationResultsMessagesCount;
    }

    public long getTotalSimulationResultsMessageLength() {
        return totalSimulatonResultsMessageLength;
    }

    public double averageSimulatonResultsMessageLength() {
        return totalSimulatonResultsMessageLength/(double)Math.max(1, simulationResultsMessagesCount);
    }
}
