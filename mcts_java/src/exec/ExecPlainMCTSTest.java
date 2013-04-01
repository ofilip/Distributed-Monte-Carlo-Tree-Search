package exec;

import exec.utils.Executor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import mcts.MCTSController;
import mcts.entries.ghosts.MCTSGhosts;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class ExecPlainMCTSTest
{
    /* usage:
     * java -jar my.jar PACMAN_CLASS GHOST_TIME SIMULATION_DEPTH UCB_COEF DEATH_WEIGHT
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        try {
            Class pacman_class = Class.forName(args[0]);
            int ghost_time = Integer.parseInt(args[1]);
            int sim_depth = Integer.parseInt(args[2]);
            double ucb_coef = Double.parseDouble(args[3]);
            double death_weight = Double.parseDouble(args[4]);
            double sim_random_prob = 1.0;
            Constructor pacman_constructor = pacman_class.getConstructor(new Class[]{});
            Controller<MOVE> pacman_controller = (Controller<MOVE>)pacman_constructor.newInstance(new Object[]{});
            MCTSGhosts ghost_controller = new MCTSGhosts(sim_depth, ucb_coef, false, sim_random_prob, death_weight);
            Executor exec = new Executor();

            Game game = new Game(System.currentTimeMillis());
            game.random_reversal = false;
            Game result = exec.runGame(game, pacman_controller, ghost_controller, false, 40, ghost_time+MCTSController.MILLIS_TO_FINISH, false);
            System.out.printf("%s\t%s\t%s\t%s\t"
                    + "%s\t%s\t%s\t%f",
                    pacman_class.getSimpleName(), MCTSGhosts.class.getSimpleName(), ghost_time, ucb_coef,
                    sim_depth, sim_random_prob, result.getScore(), ghost_controller.simulationsPerSecond());
            System.exit(0);
//        } catch (Exception ex) {
//            System.err.printf("Exception %s caught with message '%s'", ex.getClass().getSimpleName(), ex.getMessage());
//            System.exit(1);
//        }
    }
}