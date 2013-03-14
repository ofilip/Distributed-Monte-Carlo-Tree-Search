package pacman;

import java.util.ArrayList;
import java.util.List;
import pacman.entries.ghosts.generators.MCTSGhostsGenerator;
import pacman.entries.pacman.generators.StarterPacManGenerator;

public class ExecCTest {
    public static void main(String[] args) {
        MyExecutor exec = new MyExecutor();
        List<CompetitionOptions> options_list = new ArrayList<CompetitionOptions>();
        final int simulation_depth = 120;
        
        for (double ucb_coef = 0.2; ucb_coef<=2.05; ucb_coef+=0.1) {
            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, new MCTSGhostsGenerator(simulation_depth, ucb_coef), 200));
        }
        
        exec.runCompetition(options_list, 10, false, false, "d:\\pacman_test_c\\");
    }
}
