package pacman.entries.pacman.generators;

import exec.utils.PacmanControllerGenerator;
import pacman.controllers.Controller;
import pacman.entries.pacman.MCTSPacman;
import pacman.game.Constants.MOVE;

public class MCTSPacmanGenerator implements PacmanControllerGenerator {
    private int simulation_depth;
    private double ucb_coef;
    
    public MCTSPacmanGenerator(int simulation_depth, double ucb_coef) {
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
    }
    
    @Override
    public Controller<MOVE> pacmanController() {
        return new MCTSPacman(simulation_depth, ucb_coef, false);
    }

    @Override
    public String pacmanName() {
        return "MCTSPacman";
    }

}
