package exec.old;

import exec.utils.Executor;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import mcts.Constants;
import mcts.PlainMCTSController;
import mcts.entries.MCTSGhosts;
import mcts.entries.MCTSPacman;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class ExecMCTSPacmanTest
{
    /* usage:
     * java -jar my.jar PACMAN_TIME GHOSTS_CLASS UCB_COEF RANDOM_PROB DEATH_WEIGHT
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            int pacman_time = Integer.parseInt(args[0]);
            int ghost_time = 40+Constants.MILLIS_TO_FINISH;
            Class ghost_class = Class.forName(args[1]);
            int sim_depth = 120;
            double ucb_coef = Double.parseDouble(args[2]);
            double random_prob = Double.parseDouble(args[3]);
            double death_weight = Double.parseDouble(args[4]);
            double sim_random_prob = 1.0;
            Constructor ghost_constructor = ghost_class.getConstructor(new Class[]{});
            MCTSPacman pacman_controller = new MCTSPacman();
            pacman_controller.setSimulationDepth(sim_depth);
            pacman_controller.setUcbCoef(ucb_coef);
            pacman_controller.setRandomSimulationMoveProbability(random_prob);
            pacman_controller.setDeathWeight(death_weight);
            Controller<EnumMap<GHOST,MOVE>> ghost_controller = (Controller<EnumMap<GHOST,MOVE>>)ghost_constructor.newInstance(new Object[]{});
            Executor exec = new Executor();

            Game result = exec.runGame(pacman_controller, ghost_controller, false, pacman_time, ghost_time, false);
            System.out.printf("%s\t%s\t%s\t%s\t"
                    + "%s\t%s\t%s\t%s\t"
                    + "%f\t%f\t%d",
                    MCTSPacman.class.getSimpleName(), ghost_class.getSimpleName(), pacman_time, ucb_coef,
                    death_weight, sim_depth, sim_random_prob, result.getScore(),
                    pacman_controller.simulationsPerSecond(), pacman_controller.averageDecisionSimulations(), result.getTotalTime());
            System.exit(0);
        } catch (Exception ex) {
            System.err.printf("Exception %s caught with message '%s'", ex.getClass().getSimpleName(), ex.getMessage());
            System.exit(1);
        }
    }
}