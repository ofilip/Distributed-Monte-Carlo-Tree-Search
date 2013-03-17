package mcts.distributed.controller_generators;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.IndependentGhostAgent;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;


public class DummyGhostsGenerator implements GhostControllerGenerator {
    private VerboseLevel verbose;
    private int simulation_depth;
    private double ucb_coef;
    private String name;
    
    public DummyGhostsGenerator(int simulation_depth, double ucb_coef) {
        this(simulation_depth, ucb_coef, VerboseLevel.QUIET);
    }
    
    public DummyGhostsGenerator(int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        this.verbose = verbose;
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        name = String.format("DummyGhosts[simulation_depth=%d,ucb_coef=%f]", simulation_depth, ucb_coef);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        DistributedMCTSController<IndependentGhostAgent> controller = new DistributedMCTSController<IndependentGhostAgent>(1, 1, true);
        return controller.addGhostAgent(new IndependentGhostAgent(controller, GHOST.BLINKY, simulation_depth, ucb_coef, verbose))
                        .addGhostAgent(new IndependentGhostAgent(controller, GHOST.PINKY, simulation_depth, ucb_coef, verbose))
                        .addGhostAgent(new IndependentGhostAgent(controller, GHOST.INKY, simulation_depth, ucb_coef, verbose))
                        .addGhostAgent(new IndependentGhostAgent(controller, GHOST.SUE, simulation_depth, ucb_coef, verbose));                                    
    }
    
    @Override
    public String ghostName() {
        return name;
    }

}
