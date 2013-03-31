package pacman.entries.ghosts.generators;

import java.util.EnumMap;
import exec.utils.GhostControllerGenerator;
import exec.utils.PacmanControllerGenerator;
import pacman.controllers.Controller;
import mcts.entries.ghosts.MCTSGhosts;
import mcts.entries.pacman.MCTSPacman;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class MCTSGhostsGenerator implements GhostControllerGenerator   {
    private int simulation_depth;
    private double ucb_coef;
    private String name;
    private boolean verbose;
    double random_simulation_move_probability;
    double death_weight;


//    public MCTSGhostsGenerator(int simulation_depth, double ucb_coef) {
//        this(simulation_depth, ucb_coef, false, -1);
//    }

//    public MCTSGhostsGenerator(int simulation_depth, double ucb_coef, double random_simulation_move_probability) {
//        this(simulation_depth, ucb_coef, false, random_simulation_move_probability);
//    }
//
//    public MCTSGhostsGenerator(int simulation_depth, double ucb_coef, boolean verbose) {
//        this(simulation_depth, ucb_coef, verbose, -1);
//    }

    public MCTSGhostsGenerator(int simulation_depth, double ucb_coef, boolean verbose, double random_simulation_move_probability, double death_weight) {
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
//        if (random_simulation_move_probability<-0.5) {
//            this.name = String.format("MCTSGhosts[simulation_depth=%s,ucb_coef=%s]", simulation_depth, ucb_coef);
//        } else {
        this.name = String.format("MCTSGhosts[simulation_depth=%s,ucb_coef=%s,random_prob=%s]", simulation_depth, ucb_coef, random_simulation_move_probability);
//        }
        this.verbose = verbose;
        this.random_simulation_move_probability = random_simulation_move_probability;
        this.death_weight = death_weight;
    }

    @Override
    public Controller<EnumMap<GHOST,MOVE>> ghostController() {
//        if (random_simulation_move_probability<0) {
//            return new MCTSGhosts(simulation_depth, ucb_coef, verbose);
//        } else {
        return new MCTSGhosts(simulation_depth, ucb_coef, verbose, random_simulation_move_probability, death_weight);
//        }
    }

//    public MCTSGhosts ghostController(double random_simulation_move_probability) {
//        return new MCTSGhosts(simulation_depth, ucb_coef, verbose, random_simulation_move_probability);
//    }

    @Override
    public String ghostName() {
        return name;
    }

}
