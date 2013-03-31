package exec;

import pacman.controllers.ICEP_IDDFS;
import exec.utils.PacmanControllerGenerator;
import exec.utils.GhostControllerGenerator;
import communication.messages.Message;
import exec.utils.CompetitionOptions;
import exec.utils.Executor;
import exec.utils.GhostControllerGenerator;
import exec.utils.PacmanControllerGenerator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;
import java.util.Set;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import mcts.distributed.agents.JointActionExchangingAgent;
import mcts.distributed.controller_generators.DummyGhostsGenerator;
import mcts.distributed.controller_generators.JointActionExchangingGhostsGenerator;
import mcts.distributed.controller_generators.RootExchangingGhostsGenerator;
import mcts.distributed.controller_generators.SimulationResultsPassingGhostsGenerator;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.*;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import mcts.entries.ghosts.MCTSGhosts;
import mcts.entries.pacman.MCTSPacman;
import pacman.entries.ghosts.generators.GhostsGenerator;
import pacman.entries.ghosts.generators.MCTSGhostsGenerator;
import pacman.entries.pacman.generators.StarterPacManGenerator;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;
import utils.VerboseLevel;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
public class ExecPlayground
{
	public static void main(String[] args) throws NoSuchMethodException	{
            Executor exec = new Executor();
//            GhostControllerGenerator gen = new RootExchangingGhostsGenerator(200, 0.7, 10000, VerboseLevel.QUIET);
            //GhostControllerGenerator gen = new JointActionExchangingGhostsGenerator(200, 0.7, 10000, 10, VerboseLevel.DEBUGGING);
            //exec.runGameTimed(new StarterPacMan(), gen.ghostController(), true, true, 40, 1000);
            //exec.runGameTimed(new StarterPacMan(), new MCTSGhosts(200, 0.7, true), true, true, 40, 200);
            final int simulation_depth = 250;
            final double ucb_coef = 0.3;
            final long channel_transmission_speed = 10000;
            final long channel_buffer_size = 30*channel_transmission_speed; /* buffer size = 30 seconds */
//
//            PacmanControllerGenerator pgen_starter = StarterPacManGenerator.instance;
//
//            GhostControllerGenerator ggen_mcts = new MCTSGhostsGenerator(simulation_depth, ucb_coef, false);
//            GhostControllerGenerator ggen_dummy = new DummyGhostsGenerator(simulation_depth, ucb_coef);
//            GhostControllerGenerator ggen_action_exchange = new JointActionExchangingGhostsGenerator(simulation_depth, ucb_coef, channel_transmission_speed, channel_buffer_size, 5);
//            GhostControllerGenerator ggen_root_exchange = new RootExchangingGhostsGenerator(simulation_depth, ucb_coef, channel_transmission_speed, channel_buffer_size);
//            GhostControllerGenerator ggen_simulation_results_passing = new SimulationResultsPassingGhostsGenerator(simulation_depth, ucb_coef, channel_transmission_speed, channel_buffer_size);
//            GhostsGenerator ggen_legacy = new GhostsGenerator(Legacy.class);
//
//            List<CompetitionOptions> options_list = new ArrayList<CompetitionOptions>();

            //options_list.add(new CompetitionOptions(pgen_starter, 40, ggen_legacy, 40));
//            options_list.add(new CompetitionOptions(pgen_starter, 40, ggen_mcts, 400));
//            options_list.add(new CompetitionOptions(pgen_starter, 40, ggen_dummy, 400));
//            options_list.add(new CompetitionOptions(pgen_starter, 40, ggen_simulation_results_passing, 400));
//
//            runCompetition(options_list, 10, false, false, "d:\\pacman_test\\1\\");


//            exec.runGameTimed(new StarterPacMan(), ggen_simulation_results_passing.ghostController(), true, true, 40, 400);
            //exec.runGameTimed(new StarterPacMan(), ggen_mcts.ghostController(), true, true, 40, 100);
//            exec.runGameTimed(new ICEP_IDDFS(), new MCTSGhosts(simulation_depth, ucb_coef, true, 1), true, true, 40, 220);
            Game game = new Game(0);

            game.random_reversal = false;
            //game.xGetPowerPills().clear();
            //B - 1221

            exec.runGame(game , new MCTSPacman(simulation_depth, ucb_coef, 0.3, 0.1, true), new StarterGhosts(), true, 250, 40, true);
//            exec.runGame(new MCTSPacman(simulation_depth, ucb_coef, true), new Legacy(), true, 800, 40, true);


//            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_dummy, 400));
//            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_action_exchange, 400));
//            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_root_exchange, 400));
//            options_list.add(new CompetitionOptions(StarterPacManGenerator.instance, 40, gen_simulation_results_passing, 400));




//            for (int ghosts_simulation_depth: new int[]{200}) {
//                for (int ghosts_delay: new int[]{80, 200}) {
//                    for (PacmanControllerGenerator pacman_generator: new PacmanControllerGenerator[]{new StarterPacManGenerator()}) {
//                        options_list.add(new CompetitionOptions(pacman_generator, 40,
//                                            new DummyGhostsGenerator(ghosts_simulation_depth, 0.7), ghosts_delay));
//                        options_list.add(new CompetitionOptions(pacman_generator, 40,
//                                            new JointActionExchangingGhostsGenerator(ghosts_simulation_depth, 0.7, 1), ghosts_delay));
//                    }
//                }
//
//            }

//            runCompetition(options_list, 10, false, false, "d:\\pacman_test\\");
//            runCompetition(options_list, 10, false, false, "d:\\pacman_test\\");
	}

}