package mcts.distributed.controller_generators;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.*;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;


public class SimulationResultsPassingGhostsGenerator implements GhostControllerGenerator {
    private int simulation_depth;
    private double ucb_coef;
    private String name;
    VerboseLevel verbose;
    long channel_transmission_speed;
    
    public SimulationResultsPassingGhostsGenerator(int simulation_depth, double ucb_coef, long channel_transmission_speed) {
        this(simulation_depth, ucb_coef, channel_transmission_speed, VerboseLevel.QUIET);
    }
    
    public SimulationResultsPassingGhostsGenerator(int simulation_depth, double ucb_coef, long channel_transmission_speed, VerboseLevel verbose) {
        this.channel_transmission_speed = channel_transmission_speed;
        this.verbose = verbose;
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        name = String.format("JointActionExchangingGhosts[simulation_depth=%d,ucb_coef=%f]", simulation_depth, ucb_coef);
    }
    
    @Override
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        DistributedMCTSController<JointActionExchangingAgent> controller = new DistributedMCTSController<JointActionExchangingAgent>(channel_transmission_speed, true);
        
        return controller.addGhostAgent(new SimulationResultsPassingAgent(controller, GHOST.BLINKY, simulation_depth, ucb_coef, verbose))
                                .addGhostAgent(new SimulationResultsPassingAgent(controller, GHOST.PINKY, simulation_depth, ucb_coef, verbose))
                                .addGhostAgent(new SimulationResultsPassingAgent(controller, GHOST.INKY, simulation_depth, ucb_coef, verbose))
                                .addGhostAgent(new SimulationResultsPassingAgent(controller, GHOST.SUE, simulation_depth, ucb_coef, verbose));                                    
    }
    
    @Override
    public String ghostName() {
        return name;
    }

}
