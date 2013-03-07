package pacman;

import java.util.EnumMap;
import pacman.controllers.Controller;
import pacman.game.Constants;

public interface GhostControllerGenerator {
    public Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController();
    public String ghostName();
}
