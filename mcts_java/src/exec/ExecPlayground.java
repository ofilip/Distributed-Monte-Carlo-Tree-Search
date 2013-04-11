package exec;

import pacman.controllers.ICEP_IDDFS;
import exec.utils.PacmanControllerGenerator;
import exec.utils.GhostControllerGenerator;
import communication.messages.Message;
import exec.utils.CompetitionOptions;
import exec.utils.Executor;
import exec.utils.Experiment;
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
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.*;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import mcts.entries.MCTSGhosts;
import mcts.entries.MCTSPacman;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;
import pacman.game.FullGame;
import utils.VerboseLevel;


public class ExecPlayground {

    public static void main(String[] args) throws NoSuchMethodException	{
//        Controller<MOVE> pacmanController = new ICEP_IDDFS();
//        Controller<EnumMap<GHOST,MOVE>> ghostController = new MCTSGhosts(120, 0.3, true, 1, 0);
//        ghostController.set
//
//        Experiment experiment = new Experiment();
//        experiment.setPacmanController(pacmanController);
//        experiment.setGhostController(ghostController);
//        experiment.setVisual(true);
//        experiment.setGhostDelay(250);
//        experiment.execute();
    }


}