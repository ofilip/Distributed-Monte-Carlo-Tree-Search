package pacman.entries.ghosts.generators;

import java.util.EnumMap;
import pacman.GhostControllerGenerator;
import pacman.PacmanControllerGenerator;
import pacman.controllers.Controller;
import pacman.entries.ghosts.MCTSGhosts;
import pacman.entries.pacman.MCTSPacman;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class MCTSGhostsGenerator implements GhostControllerGenerator   {
    private int simulation_depth;
    private double ucb_coef;
    private String name;
    
    public MCTSGhostsGenerator(int simulation_depth, double ucb_coef) {
        this.simulation_depth = simulation_depth;
        this.ucb_coef = ucb_coef;
        this.name = String.format("MCTSGhosts[simulation_depth=%s,ucb_coef=%s]", simulation_depth, ucb_coef);
    }
    
    @Override
    public Controller<EnumMap<GHOST,MOVE>> ghostController() {
        return new MCTSGhosts(simulation_depth, ucb_coef, false);
    }

    @Override
    public String ghostName() {
        return name;
    }   

}
