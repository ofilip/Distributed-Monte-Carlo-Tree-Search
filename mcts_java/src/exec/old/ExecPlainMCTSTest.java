package exec.old;

import exec.utils.Executor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import mcts.PlainMCTSController;
import mcts.entries.MCTSGhosts;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.FullGame;
import pacman.game.Game;


public class ExecPlainMCTSTest
{
    /* usage:
     * java -jar my.jar PACMAN_CLASS GHOST_TIME SIMULATION_DEPTH UCB_COEF DEATH_WEIGHT
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Class pacman_class = Class.forName(args[0]);
        int ghost_time = Integer.parseInt(args[1]);
        int sim_depth = Integer.parseInt(args[2]);
        double ucb_coef = Double.parseDouble(args[3]);
        double death_weight = Double.parseDouble(args[4]);
        double sim_random_prob = 1.0;
        Constructor pacman_constructor = pacman_class.getConstructor(new Class[]{});
        Controller<MOVE> pacman_controller = (Controller<MOVE>)pacman_constructor.newInstance(new Object[]{});
        MCTSGhosts ghost_controller = new MCTSGhosts();
        ghost_controller.setSimulationDepth(sim_depth);
        ghost_controller.setUcbCoef(ucb_coef);
        ghost_controller.setRandomSimulationMoveProbability(sim_random_prob);
        ghost_controller.setDeathWeight(death_weight);
        Executor exec = new Executor();

        FullGame game = new FullGame(System.currentTimeMillis());
        game.random_reversal = false;
        Game result = exec.runGame(game, pacman_controller, ghost_controller, false, 40, ghost_time, false);
        System.out.printf("%s\t%s\t%s\t%s\t"
                + "%s\t%s\t%s\t%f\t"
                +"%f\t%d",
                pacman_class.getSimpleName(), MCTSGhosts.class.getSimpleName(), ghost_time, ucb_coef,
                sim_depth, sim_random_prob, result.getScore(), ghost_controller.simulationsPerSecond(),
                ghost_controller.averageDecisionSimulations(), result.getTotalTime());
            System.exit(0);
    }
}