package exec;

import exec.utils.CompetitionOptions;
import exec.utils.Executor;
import exec.utils.PacmanControllerGenerator;
import exec.utils.GhostControllerGenerator;
import java.util.ArrayList;
import java.util.List;
import mcts.distributed.controller_generators.DummyGhostsGenerator;
import mcts.distributed.controller_generators.JointActionExchangingGhostsGenerator;
import mcts.distributed.controller_generators.RootExchangingGhostsGenerator;
import mcts.distributed.controller_generators.SimulationResultsPassingGhostsGenerator;
import pacman.controllers.examples.*;
import pacman.entries.ghosts.generators.GhostsGenerator;
import pacman.entries.ghosts.generators.MCTSGhostsGenerator;
import pacman.entries.pacman.generators.StarterPacManGenerator;

public class ExecSimulationTuning {
    public static void main(String[] args) throws NoSuchMethodException {
        final int simulation_depth = 120;
        final double ucb_coef = 0.7;
        final int ghosts_delay = 400;
        List<CompetitionOptions> options_list = new ArrayList<CompetitionOptions>();
        PacmanControllerGenerator pgen_starter = StarterPacManGenerator.instance;

        for (double random_move_prob = 0.1; random_move_prob<0.95; random_move_prob += 0.1) {
            GhostControllerGenerator ggen = new MCTSGhostsGenerator(simulation_depth, ucb_coef, false, random_move_prob);
            options_list.add(new CompetitionOptions(pgen_starter, 40, ggen, ghosts_delay));
        }

        Executor.runCompetition(options_list, 20, false, false, "d:\\pacman_test\\simulation_tuning\\");
    }
}