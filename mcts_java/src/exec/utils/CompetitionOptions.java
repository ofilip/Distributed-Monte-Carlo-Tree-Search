package exec.utils;

import exec.utils.PacmanControllerGenerator;
import exec.utils.GhostControllerGenerator;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import exec.utils.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

public class CompetitionOptions implements PacmanControllerGenerator, GhostControllerGenerator {
    private int pacman_delay;
    private int ghosts_delay;
    PacmanControllerGenerator pacman_generator;
    GhostControllerGenerator ghost_generator;

    public CompetitionOptions(PacmanControllerGenerator pacman_generator, int pacman_delay, GhostControllerGenerator ghost_generator, int ghosts_delay) {
        this.pacman_delay = pacman_delay;
        this.pacman_generator = pacman_generator;
        this.ghosts_delay = ghosts_delay;
        this.ghost_generator = ghost_generator;
    }
           
    @Override
    public Controller<Constants.MOVE> pacmanController() {
        return pacman_generator.pacmanController();
    }
    @Override
    public Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController() {
        return ghost_generator.ghostController();
    }
    public int pacmanDelay() {
        return pacman_delay;
    }
    public int ghostsDelay() {
        return ghosts_delay;
    }
    @Override
    public String pacmanName() { return pacman_generator.pacmanName(); }
    @Override
    public String ghostName() { return ghost_generator.ghostName(); }
}
    
