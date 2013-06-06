package exec;

import exec.utils.Experiment;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import mcts.Constants;
import mcts.MCTSController;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.entries.RootExchangingGhosts;
import mcts.distributed.entries.SimulationResultsPassingGhosts;
import mcts.distributed.entries.TreeCutExchangingGhosts;
import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.SimplifiedGame;
import utils.VerboseLevel;


enum Option {
    PACMAN_CLASS("pacman-class"),
    PACMAN_TIME("pacman-time"),
    PACMAN_SIMULATION_DEPTH("pacman-simdepth"),
    PACMAN_UCB_COEF("pacman-ucb-coef"),
    PACMAN_RANDOM_PROB("pacman-random-prob"),
    PACMAN_DEATH_WEIGHT("pacman-death-weight"),
    GHOST_CLASS("ghost-class"),
    GHOST_TIME("ghost-time"),
    GHOST_SIMULATION_DEPTH("ghost-simdepth"),
    GHOST_UCB_COEF("ghost-ucb-coef"),
    GHOST_RANDOM_PROB("ghost-random-prob"),
    GHOST_DEATH_WEIGHT("ghost-death-weight"),
    CHANNEL_SPEED("channel-speed"),
    CUTS_PER_TICK("cuts-per-tick"),
    GAME_LENGTH("game-length"),
    MULTITHREADED("multithreaded", LongOpt.NO_ARGUMENT),
    TRIAL_NO("trial-no"),
    PESIMISTIC_TURNS("pesimistic-turns", LongOpt.NO_ARGUMENT),
    VISUAL("visual", LongOpt.NO_ARGUMENT),
    VERBOSE("verbose", LongOpt.NO_ARGUMENT),
    DEBUG("debug", LongOpt.NO_ARGUMENT),
    HEADER("header", LongOpt.NO_ARGUMENT),
    SHORT_LAIR_TIME("short-lair-time", LongOpt.NO_ARGUMENT),
    WITH_HEADER("with-header", LongOpt.NO_ARGUMENT);


    private LongOpt longopt;
    public final static LongOpt LONG_OPTIONS[];
    static {
        Option options[] = Option.values();
        LONG_OPTIONS = new LongOpt[options.length];
        for (int i=0; i<options.length; i++) {
            LONG_OPTIONS[i] = options[i].getLongopt();
        }
    }

    Option(String name) {
        this(name, LongOpt.REQUIRED_ARGUMENT);
    }

    Option(String name, int has_arg) {
        longopt = new LongOpt(name, has_arg, null, this.ordinal());
    }

    /**
     * @return the longopt
     */
    public LongOpt getLongopt() {
        return longopt;
    }

    /**
     * @param longopt the longopt to set
     */
    public void setLongopt(LongOpt longopt) {
        this.longopt = longopt;
    }

}

public class ExecExperiment {
    private final static String[] CONTROLLER_PACKAGES = {
        "pacman.controllers.examples",
        "pacman.controllers",
        "mcts.entries",
        "mcts.distributed.entries",
    };

    private static Class lookupClass(String className) throws ClassNotFoundException {
        if (className.contains(".")) {
            return Class.forName(className);
        } else {
            for (String packageName: CONTROLLER_PACKAGES) {
                try {
                    return Class.forName(String.format("%s.%s", packageName, className));
                } catch (ClassNotFoundException ex) {
                    continue;
                }
            }
            throw new ClassNotFoundException(className);
        }
    }

    private static boolean isDefault(String param) {
        return param.toLowerCase().equals("default");
    }

    private static <T> Controller<T> buildController(Class c, int simulationDepth, double ucbCoef, double randomProb, double deathWeight)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        return buildController(c, simulationDepth, ucbCoef, randomProb, deathWeight, 0, 0, 0, false, true, VerboseLevel.QUIET);
    }

    @SuppressWarnings("unchecked")
    private static <T> Controller<T> buildController(Class c, int simulationDepth, double ucbCoef, double randomProb, double deathWeight, long tickLength, long channelSpeed, double cutsPerSecond, boolean multithreaded, boolean optimisticTurns, VerboseLevel verboseLevel)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor constructor = c.getConstructor();
        Controller<T> controller = (Controller<T>)constructor.newInstance();

        if (controller instanceof MCTSController) {
            MCTSController mctsController = (MCTSController)controller;
            mctsController.setSimulationDepth(simulationDepth);
            mctsController.setUcbCoef(ucbCoef);
            mctsController.setRandomSimulationMoveProbability(randomProb);
            mctsController.setDeathWeight(deathWeight);
            mctsController.setOptimisticTurns(optimisticTurns);
            mctsController.setVerboseLevel(verboseLevel);
        }

        if (controller instanceof DistributedMCTSController) {
            DistributedMCTSController dmctsController = (DistributedMCTSController)controller;

            dmctsController.getNetwork().setChannelTransmissionSpeed(channelSpeed);
            dmctsController.setMultithreaded(multithreaded);
        }

        if (controller instanceof TreeCutExchangingGhosts) {
            TreeCutExchangingGhosts cutGhosts = (TreeCutExchangingGhosts)controller;
            cutGhosts.setCutsSentByTick(cutsPerSecond, tickLength, channelSpeed);
        }

        return controller;
    }

    private static void printControllerHeader(String prefix, Controller controller) {
        System.out.printf("%sclass\t%stime\t", prefix, prefix);
        if (controller instanceof MCTSController) {
            System.out.printf("%sreal_time\t%ssim_depth\t%sucb_coef\t%sdeath_weight\t%savg_decision_sims\t%ssims_per_sec\t%soptimistic_turns\t",
                              prefix, prefix, prefix, prefix, prefix, prefix, prefix);
        }
        if (controller instanceof DistributedMCTSController) {
            System.out.printf("sims_per_sec_calculated\tsims_per_sec_total\tchannel_speed\ttransmitted_per_second_total\ttransmitted_per_second_successfully\tsynchronization_ratio\t");
        }
        if (controller instanceof SimulationResultsPassingGhosts) {
            System.out.printf("average_simulation_message_length\ttransmitted_simulations_ratio\t");
        }
        if (controller instanceof RootExchangingGhosts) {
            System.out.printf("root_size_ratio\t");
        }
        if (controller instanceof TreeCutExchangingGhosts) {
            System.out.printf("cuts_per_second\t");
        }
    }

    private static void printHeader(Controller<MOVE> pacmanController, Controller<EnumMap<GHOST,MOVE>> ghostController) {
        System.out.printf("trial\t");
        printControllerHeader("pacman_", pacmanController);
        printControllerHeader("ghost_", ghostController);
        System.out.printf("score\n");
    }

    private static void printControllerInfo(Controller controller, int time, double cutsPerTick) {
        System.out.printf("%s\t%s\t", controller.getClass().getSimpleName(), time);
        if (controller instanceof MCTSController) {
            MCTSController mctsController = (MCTSController)controller;
            System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t",
                             mctsController.millisPerMove(), mctsController.getSimulationDepth(), mctsController.getUcbCoef(),
                             mctsController.getDeathWeight(), mctsController.averageDecisionSimulations(),
                             mctsController.simulationsPerSecond(), (mctsController.getOptimisticTurns()? "true": "false"));
        }
        if (controller instanceof DistributedMCTSController) {
            DistributedMCTSController dmctsController = (DistributedMCTSController)controller;
            System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t", dmctsController.calculatedSimulationsPerSecond(), dmctsController.totalSimulationsPerSecond(),
                    dmctsController.getNetwork().getChannelTransmissionSpeed(),
                    dmctsController.transmittedTotalPerSecond(), dmctsController.transmittedSuccessfullyPerSecond(),
                    dmctsController.coordinatedDecisionsRatio());
        }
        if (controller instanceof SimulationResultsPassingGhosts) {
            SimulationResultsPassingGhosts ghostsController = (SimulationResultsPassingGhosts)controller;
            System.out.printf("%s\t%s\t", ghostsController.averageSimulatonResultsMessageLength(), ghostsController.transmittedSimulationsRatio());
        }
        if (controller instanceof RootExchangingGhosts) {
            RootExchangingGhosts rootController = (RootExchangingGhosts)controller;
            System.out.printf("%s\t", rootController.rootSizeRatio());
        }
        if (controller instanceof TreeCutExchangingGhosts) {
            System.out.printf("%s\t", cutsPerTick);
        }
    }

    private static void printResults(int trialNo, Experiment experiment, Controller<MOVE> pacmanController,
                                     Controller<EnumMap<GHOST,MOVE>> ghostController, Game result, double cutsPerTick) {
        System.out.printf("%s\t", trialNo);
        printControllerInfo(pacmanController, experiment.getPacmanDelay(), cutsPerTick);
        printControllerInfo(ghostController, experiment.getGhostDelay(), cutsPerTick);
        System.out.printf("%s\n", result.getScore());
    }

    public static void main(String[] args) throws Exception {
        int trialNo = 1;
        boolean header = false;
        boolean dontRun = false;

        Class pacmanClass = StarterPacMan.class;
        int pacmanSimulationDepth = Constants.DEFAULT_SIMULATION_DEPTH;
        double pacmanUcbCoef = Constants.DEFAULT_UCB_COEF;
        double pacmanRandomProb = Constants.DEFAULT_RANDOM_PROB;
        double pacmanDeathWeight = Constants.DEFAULT_DEATH_WEIGHT;
        boolean multithreaded = false;

        Class ghostClass = StarterGhosts.class;
        int ghostSimulationDepth = Constants.DEFAULT_SIMULATION_DEPTH;
        double ghostUcbCoef = Constants.DEFAULT_UCB_COEF;
        double ghostRandomProb = Constants.DEFAULT_RANDOM_PROB;
        Experiment experiment = new Experiment();
        SimplifiedGame game = new SimplifiedGame(System.currentTimeMillis());
        double ghostDeathWeight = Constants.DEFAULT_DEATH_WEIGHT;
        long channelSpeed = Constants.DEFAULT_CHANNEL_TRANSMISSION_SPEED;
        double cutsPerTick = Constants.DEFAULT_CUTS_PER_TICK;
        boolean optimisticTurns = true;
        VerboseLevel verboseLevel = VerboseLevel.QUIET;

        Getopt getopt = new Getopt(ExecExperiment.class.getSimpleName(), args, "", Option.LONG_OPTIONS);
        int c;

        while ((c = getopt.getopt())!=-1) {
            Option option = Option.values()[c];

            switch (option) {
                case PACMAN_CLASS:
                    if (!isDefault(getopt.getOptarg())) {
                        pacmanClass = lookupClass(getopt.getOptarg());
                    }
                    break;
                case PACMAN_TIME:
                    if (!isDefault(getopt.getOptarg())) {
                        experiment.setPacmanDelay(Integer.parseInt(getopt.getOptarg()));
                    }
                    break;
                case PACMAN_SIMULATION_DEPTH:
                    if (!isDefault(getopt.getOptarg())) {
                        pacmanSimulationDepth = Integer.parseInt(getopt.getOptarg());
                    }
                    break;
                case PACMAN_UCB_COEF:
                    pacmanUcbCoef = Double.parseDouble(getopt.getOptarg());
                    break;
                case PACMAN_RANDOM_PROB:
                    pacmanRandomProb = Double.parseDouble(getopt.getOptarg());
                    break;
                case PACMAN_DEATH_WEIGHT:
                    pacmanDeathWeight = Double.parseDouble(getopt.getOptarg());
                    break;
                case GHOST_CLASS:
                    ghostClass = lookupClass(getopt.getOptarg());
                    break;
                case GHOST_TIME:
                    experiment.setGhostDelay(Integer.parseInt(getopt.getOptarg()));
                    break;
                case GHOST_SIMULATION_DEPTH:
                    ghostSimulationDepth = Integer.parseInt(getopt.getOptarg());
                    break;
                case GHOST_UCB_COEF:
                    ghostUcbCoef = Double.parseDouble(getopt.getOptarg());
                    break;
                case GHOST_RANDOM_PROB:
                    ghostRandomProb = Double.parseDouble(getopt.getOptarg());
                    break;
                case GHOST_DEATH_WEIGHT:
                    ghostDeathWeight = Double.parseDouble(getopt.getOptarg());
                    break;
                case PESIMISTIC_TURNS:
                    optimisticTurns = false;
                    break;
                case CHANNEL_SPEED:
                    channelSpeed = Long.parseLong(getopt.getOptarg());
                    break;
                case CUTS_PER_TICK:
                    cutsPerTick = Double.parseDouble(getopt.getOptarg());
                    break;
                case GAME_LENGTH:
                    game.setGameLength(Integer.parseInt(getopt.getOptarg()));
                    break;
                case MULTITHREADED:
                    experiment.setMultithreaded(true);
                    multithreaded = true;
                    break;
                case VISUAL:
                    experiment.setVisual(true);
                    break;
                case VERBOSE:
                    verboseLevel = VerboseLevel.VERBOSE;
                    break;
                case DEBUG:
                    verboseLevel = VerboseLevel.DEBUGGING;
                    break;
                case HEADER:
                    header = true;
                    dontRun = true;
                    break;
                case WITH_HEADER:
                    header = true;
                    break;
                case TRIAL_NO:
                    trialNo = Integer.parseInt(getopt.getOptarg());
                    break;
                case SHORT_LAIR_TIME:
                    game.setShortLairTimes();
                    break;
                default:
                    System.err.printf("Unhandled switch: %s\n", option.getLongopt().getFlag());
                    System.exit(1);
                    break;
            }
        }

        Controller<MOVE> pacmanController = buildController(pacmanClass, pacmanSimulationDepth, pacmanUcbCoef, pacmanRandomProb, pacmanDeathWeight);
        Controller<EnumMap<GHOST,MOVE>> ghostController = buildController(ghostClass, ghostSimulationDepth, ghostUcbCoef, ghostRandomProb, ghostDeathWeight, experiment.getGhostDelay(),
                channelSpeed, cutsPerTick, multithreaded, optimisticTurns, verboseLevel);

        if (!dontRun) {
            experiment.setPacmanController(pacmanController);
            experiment.setGhostController(ghostController);
            experiment.setGame(game);

            Game result = experiment.execute();
            if (header) {
                printHeader(pacmanController, ghostController);
            }
            printResults(trialNo, experiment, pacmanController, ghostController, result, cutsPerTick);
        } else if (header) {
            printHeader(pacmanController, ghostController);
        }
    }
}