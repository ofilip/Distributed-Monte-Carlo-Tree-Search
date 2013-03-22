package exec;

import exec.utils.Executor;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import mcts.MCTSController;
import mcts.entries.ghosts.MCTSGhosts;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class ExecExapleGhostsTest
{
    /* usage:
     * java -jar my.jar PACMAN_CLASS GHOST_CLASS
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            Class pacman_class = Class.forName(args[0]);
            Constructor pacman_constructor = pacman_class.getConstructor(new Class[]{});
            Controller<MOVE> pacman_controller = (Controller<MOVE>)pacman_constructor.newInstance(new Object[]{});

            Class ghost_class = Class.forName(args[1]);
            Constructor ghost_constructor = ghost_class.getConstructor(new Class[]{});
            Controller<EnumMap<GHOST,MOVE>> ghost_controller = (Controller<EnumMap<GHOST,MOVE>>) ghost_constructor.newInstance(new Object[]{});

            Executor exec = new Executor();

            Game result = exec.runGame(pacman_controller, ghost_controller, false, 40, 40, false);
            System.out.printf("%s\t%s\t%s", pacman_class.getSimpleName(), ghost_class.getSimpleName(), result.getScore());
            System.exit(0);
        } catch (Exception ex) {
            System.err.printf("Exception %s caught with message '%s'", ex.getClass().getSimpleName(), ex.getMessage());
            System.exit(1);
        }
    }
}