package mcts.distributed.controller_generators;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.IndependentGhostAgent;
import mcts.distributed.agents.JointActionExchangingAgent;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;


public class JointActionExchangingGhostsGenerator implements GhostControllerGenerator {
    private long channel_transmission_speed;
    private int simulation_depth;
    private double ucb_coef;
    private int moves_message_interval;
    private String name;
    private VerboseLevel verbose = VerboseLevel.QUIET;
    
    public JointActionExchangingGhostsGenerator(int simulation_depth, double ucb_coef, long channel_transmission_speed, int moves_message_interval) {
        this(simulation_depth, ucb_coef, channel_transmission_speed, moves_message_interval, VerboseLevel.QUIET);
    }
    
    public JointActionExchangingGhostsGenerator(int simulation_depth, double ucb_coef, long channel_transmission_speed, int moves_message_interval, VerboseLevel verbose) {
        this.channel_transmission_speed = channel_transmission_speed;
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        this.moves_message_interval = moves_message_interval;
        this.name = String.format("JointActionExchangingGhosts[transmission_speed=%d,simulation_depth=%d,ucb_coef=%f,moves_message_interval=%d]", channel_transmission_speed, simulation_depth, ucb_coef, moves_message_interval);
        this.verbose = verbose;
    }
    
    @Override
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        DistributedMCTSController<JointActionExchangingAgent> controller = new DistributedMCTSController<JointActionExchangingAgent>(channel_transmission_speed, true);
        
        return controller.addGhostAgent(new JointActionExchangingAgent(controller, GHOST.BLINKY, simulation_depth, ucb_coef, moves_message_interval, verbose))
                                .addGhostAgent(new JointActionExchangingAgent(controller, GHOST.PINKY, simulation_depth, ucb_coef, moves_message_interval, verbose))
                                .addGhostAgent(new JointActionExchangingAgent(controller, GHOST.INKY, simulation_depth, ucb_coef, moves_message_interval, verbose))
                                .addGhostAgent(new JointActionExchangingAgent(controller, GHOST.SUE, simulation_depth, ucb_coef, moves_message_interval, verbose));                                    
    }
    
    @Override
    public String ghostName() {
        return name;
    }

}
