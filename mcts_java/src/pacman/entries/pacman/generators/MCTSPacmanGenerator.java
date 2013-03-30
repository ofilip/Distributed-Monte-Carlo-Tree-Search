package pacman.entries.pacman.generators;

import exec.utils.PacmanControllerGenerator;
import pacman.controllers.Controller;
import mcts.entries.pacman.MCTSPacman;
import pacman.game.Constants.MOVE;

public class MCTSPacmanGenerator implements PacmanControllerGenerator {
    private int simulation_depth;
    private double ucb_coef;
    double random_simulation_move_probability;
    double death_weigth;

    public MCTSPacmanGenerator(int simulation_depth, double ucb_coef, double random_simulation_move_probability, double death_weight) {
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        this.random_simulation_move_probability = random_simulation_move_probability;
        this.death_weigth = death_weight;
    }

    @Override
    public Controller<MOVE> pacmanController() {
        return new MCTSPacman(simulation_depth, ucb_coef, random_simulation_move_probability, death_weigth, false);
    }

    @Override
    public String pacmanName() {
        return "MCTSPacman";
    }

}
