package mcts.distributed.controller_generators;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import mcts.distributed.agents.JointActionExchangingAgent;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;


public class JointActionExchangingGhostsGenerator implements GhostControllerGenerator {
    private int simulation_depth;
    private double ucb_coef;
    private int moves_message_interval;
    private String name;
    
    public JointActionExchangingGhostsGenerator(int simulation_depth, double ucb_coef, int moves_message_interval) {
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        this.moves_message_interval = moves_message_interval;
        name = String.format("JointActionExchangingGhosts[simulation_depth=%d,ucb_coef=%f,moves_message_interval=%d]", simulation_depth, ucb_coef, moves_message_interval);
    }
    
    @Override
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        DistributedMCTSController<JointActionExchangingAgent> controller = new DistributedMCTSController<JointActionExchangingAgent>(1, true);
        
        return controller.addGhostAgent(new JointActionExchangingAgent(controller, GHOST.BLINKY, simulation_depth, ucb_coef, moves_message_interval))
                                .addGhostAgent(new JointActionExchangingAgent(controller, GHOST.PINKY, simulation_depth, ucb_coef, moves_message_interval))
                                .addGhostAgent(new JointActionExchangingAgent(controller, GHOST.INKY, simulation_depth, ucb_coef, moves_message_interval))
                                .addGhostAgent(new JointActionExchangingAgent(controller, GHOST.SUE, simulation_depth, ucb_coef, moves_message_interval));                                    
    }
    
    @Override
    public String ghostName() {
        return name;
    }

}
