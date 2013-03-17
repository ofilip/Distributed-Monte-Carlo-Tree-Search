package mcts.distributed.controller_generators;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.*;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;


public class RootExchangingGhostsGenerator implements GhostControllerGenerator {
    private int simulation_depth;
    private double ucb_coef;
    private String name;
    VerboseLevel verbose;
    long channel_transmission_speed;
    long channel_buffer_size;
    
    public RootExchangingGhostsGenerator(int simulation_depth, double ucb_coef, long channel_transmission_speed, long channel_buffer_size) {
        this(simulation_depth, ucb_coef, channel_transmission_speed, channel_buffer_size, VerboseLevel.QUIET);
    }
    
    public RootExchangingGhostsGenerator(int simulation_depth, double ucb_coef, long channel_transmission_speed, long channel_buffer_size, VerboseLevel verbose) {
        this.channel_transmission_speed = channel_transmission_speed;
        this.verbose = verbose;
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        this.name = String.format("JointActionExchangingGhosts[simulation_depth=%d,ucb_coef=%f]", simulation_depth, ucb_coef);
        this.channel_buffer_size = channel_buffer_size;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        DistributedMCTSController<JointActionExchangingAgent> controller = new DistributedMCTSController<JointActionExchangingAgent>(channel_transmission_speed, channel_buffer_size, true);
        
        return controller.addGhostAgent(new RootExchangingAgent(controller, GHOST.BLINKY, simulation_depth, ucb_coef, verbose))
                                .addGhostAgent(new RootExchangingAgent(controller, GHOST.PINKY, simulation_depth, ucb_coef, verbose))
                                .addGhostAgent(new RootExchangingAgent(controller, GHOST.INKY, simulation_depth, ucb_coef, verbose))
                                .addGhostAgent(new RootExchangingAgent(controller, GHOST.SUE, simulation_depth, ucb_coef, verbose));                                    
    }
    
    @Override
    public String ghostName() {
        return name;
    }

}
