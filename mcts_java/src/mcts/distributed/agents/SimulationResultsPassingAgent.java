package mcts.distributed.agents;

import communication.messages.Message;
import communication.messages.RootMessage;
import communication.messages.SimulationResultMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mcts.Action;
import mcts.GhostsNode;
import mcts.MCNode;
import mcts.PacmanNode;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.IntervalHistory;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;
import utils.VerboseLevel;

public class SimulationResultsPassingAgent extends FullMCTSGhostAgent {   
    
    public SimulationResultsPassingAgent(DistributedMCTSController controller, final GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
        
        hookMessageHandler(SimulationResultMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
                SimulationResultMessage result_message = (SimulationResultMessage)message;
                try {
                    mctree.applySimulationResult(result_message.treeMoves(), result_message.simulationResult());
                } catch (InvalidActionListException e) { assert(false); }
            }
        });
    }
    
    public SimulationResultsPassingAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef) {
        this(controller, ghost, simulation_depth, ucb_coef, VerboseLevel.QUIET);
    }
    
    private void sendMessages(List<Action> action_list, double simulation_result) {
        //TODO:
        // * aggregated information passing
        // * load ballancing
        // * move message (send prioritized)
        // * push message at the beginning of messge queue
        SimulationResultMessage message = new SimulationResultMessage(action_list, simulation_result);
        broadcastMessage(message);
    }

    @Override
    public void step() {
        List<Action> action_list = new ArrayList<Action>();
        
        receiveMessages();
        double simulation_result = mctree.iterate(action_list);
        sendMessages(action_list, simulation_result);
    }

    @Override
    public MOVE getMove() {
        //TODO: interchange move messages
        return mctree.bestMove(current_game).get(ghost);
    }

}
