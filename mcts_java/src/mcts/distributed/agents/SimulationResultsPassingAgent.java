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
    private int MOVE_MESSAGE_INTERVAL = 50;
    private final Map<GHOST, MoveMessage> receivedMoves = new EnumMap<GHOST, MoveMessage>(GHOST.class);
    private long totalSimulations = 0;
    private long totalSimulatonResultsMessageLength = 0;
    private long simulationResultsMessagesCount = 0;
    private EnumMap<GHOST, MOVE> lastBestMove = Utils.NEUTRAL_GHOSTS_MOVES; /* best move during last message sending */
    private long lastBestMoveSendTime = -MOVE_MESSAGE_INTERVAL;

    public SimulationResultsPassingAgent(DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);

        hookMoveMessageHandler(receivedMoves);

        hookMessageHandler(SimulationResultMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                SimulationResultMessage result_message = (SimulationResultMessage)message;
                try {
                    totalSimulations++;
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
        message.onSendingStarted(new MessageCallback() {
            public void call(Message message) {
                totalSimulatonResultsMessageLength += message.length();
                simulationResultsMessagesCount++;
            }
        });

//        System.err.printf("[%s:%s] broadcasting %s\n", ghost, controller.currentVirtualMillis(), message);

        broadcastMessage(Priority.MEDIUM, message, true);

        /* Send MOVE message if current best move changes (but keep minimal interval between
         * two sent messages).
         */
        EnumMap<GHOST,MOVE> currentBestMove = mctree.bestDecisionMove();
        if (lastBestMoveSendTime+MOVE_MESSAGE_INTERVAL>controller.currentVirtualMillis()
                &&!Utils.ghostMovesEqual(currentBestMove,Utils.NEUTRAL_GHOSTS_MOVES)
                &&!Utils.ghostMovesEqual(lastBestMove, currentBestMove)) {
            lastBestMoveSendTime = controller.currentVirtualMillis();
            lastBestMove = currentBestMove;
            broadcastMoveMessage(Priority.HIGH, currentBestMove);
        }
    }

    @Override
    public void step() {
        List<Action> action_list = new ArrayList<Action>();

        receiveMessages();
        double simulation_result = mctree.iterate(action_list);
        if (!Double.isNaN(simulation_result)) {
            totalSimulations++;
            sendMessages(action_list, simulation_result);
        }
    }

    @Override
    public MOVE getMove() {
        return getMoveFromMessages(receivedMoves/*, mctree.bestDecisionMove()*/);
    }

    @Override
    public long totalSimulations() {
        return totalSimulations;
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
