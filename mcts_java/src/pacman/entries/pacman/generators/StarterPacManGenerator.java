package pacman.entries.pacman.generators;

import pacman.PacmanControllerGenerator;
import pacman.controllers.Controller;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Constants.MOVE;

public class StarterPacManGenerator implements PacmanControllerGenerator {

    @Override
    public Controller<MOVE> pacmanController() {
        return new StarterPacMan();
    }

    @Override
    public String pacmanName() {
        return "StarterPacMan";
    }

}