package exec.utils;

import pacman.controllers.Controller;
import pacman.game.Constants;

public interface PacmanControllerGenerator {
    public Controller<Constants.MOVE> pacmanController();
    public String pacmanName();
}
