package exec;

import exec.utils.Executor;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import mcts.MCTSController;
import mcts.entries.ghosts.MCTSGhosts;
import mcts.entries.pacman.MCTSPacman;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class ExecMCTSPacmanTest
{
    /* usage:
     * java -jar my.jar PACMAN_TIME GHOSTS_CLASS UCB_COEF
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            int pacman_time = Integer.parseInt(args[0]);
            int ghost_time = 40+MCTSController.MILLIS_TO_FINISH;
            Class ghost_class = Class.forName(args[1]);
            int sim_depth = 120;
            double ucb_coef = Double.parseDouble(args[2]);
            double sim_random_prob = 1.0;
            Constructor ghost_constructor = ghost_class.getConstructor(new Class[]{});
            MCTSPacman pacman_controller = new MCTSPacman(sim_depth, ucb_coef, false);
            Controller<EnumMap<GHOST,MOVE>> ghost_controller = (Controller<EnumMap<GHOST,MOVE>>)ghost_constructor.newInstance(new Object[]{});
            Executor exec = new Executor();

            Game result = exec.runGame(pacman_controller, ghost_controller, false, pacman_time+MCTSController.MILLIS_TO_FINISH, ghost_time, false);
            System.out.printf("%s\t%s\t%s\t%s\t"
                    + "%s\t%s\t%s\t%f",
                    MCTSPacman.class.getSimpleName(), ghost_class.getSimpleName(), ghost_time, ucb_coef,
                    sim_depth, sim_random_prob, result.getScore(), pacman_controller.simulationsPerSecond());
            System.exit(0);
        } catch (Exception ex) {
            System.err.printf("Exception %s caught with message '%s'", ex.getClass().getSimpleName(), ex.getMessage());
            System.exit(1);
        }
    }
}