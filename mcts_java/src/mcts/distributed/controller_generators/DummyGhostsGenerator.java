package mcts.distributed.controller_generators;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import pacman.GhostControllerGenerator;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;


public class DummyGhostsGenerator implements GhostControllerGenerator {
    private int simulation_depth;
    private double ucb_coef;
    private String name;
    
    public DummyGhostsGenerator(int simulation_depth, double ucb_coef) {
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        name = String.format("DummyGhosts[simulation_depth=%d,ucb_coef=%f]", simulation_depth, ucb_coef);
    }
    
    @Override
    public Controller<EnumMap<GHOST, MOVE>> ghostController() {
        DistributedMCTSController<DummyGhostAgent> controller = new DistributedMCTSController<DummyGhostAgent>(1, true);
                
        return controller.addGhostAgent(new DummyGhostAgent(controller, GHOST.BLINKY, simulation_depth, ucb_coef, false))
                        .addGhostAgent(new DummyGhostAgent(controller, GHOST.PINKY, simulation_depth, ucb_coef, false))
                        .addGhostAgent(new DummyGhostAgent(controller, GHOST.INKY, simulation_depth, ucb_coef, false))
                        .addGhostAgent(new DummyGhostAgent(controller, GHOST.SUE, simulation_depth, ucb_coef, false));                                    
    }
    
    @Override
    public String ghostName() {
        return name;
    }

}
