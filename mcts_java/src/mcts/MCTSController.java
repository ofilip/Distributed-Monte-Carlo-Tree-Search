package mcts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import mcts.Utils;
import pacman.controllers.Controller;
import pacman.game.Game;

public abstract class MCTSController<T extends MCTree<M>, M> extends Controller<M> implements SimulationsStat, TreeSimulationsStat {
    protected T mctree = null;
    protected int currentLevel;
    protected Backpropagator backpropagator = AvgBackpropagator.getInstance();
    protected GuidedSimulator guidedSimulator = new GuidedSimulator(System.currentTimeMillis());
    protected UCBSelector ucbSelector = new UCBSelector(30, guidedSimulator);

    protected Game previous_game = null;
    protected int pacman_decision_gap = 1;
    protected M last_move;
    protected long total_simulations = 0;
    protected long current_simulations = 0;
    protected List<Long> decision_simulations = new ArrayList<Long>();
    protected long total_time_millis = 0;
    protected long decisions = 0;

    private boolean verbose = false;
    private double ucbCoef = 0.3;
//    private double deathWeight = 1.0;
//    private int simulationDepth = 120;
//    private double randomSimulationMoveProbability = 1.0;

    public static final long DEFAULT_TIME_MILLIS = 100;
    public static final int MILLIS_TO_FINISH = 0;

//    public MCTSController(int simulation_depth, double ucbCoef, boolean verbose) {
//        this(simulation_depth, ucbCoef, verbose, DEFAULT_ITERATION_COUNT, -1);
//    }
    public MCTSController() {

    }

    public boolean isVerbose() { return verbose; }
    public void setVerbose(boolean verbose) { this.verbose = verbose; }

    /**
     * @return the ucbCoef
     */
    public double getUcbCoef() {
        return ucbCoef;
    }

    /**
     * @param ucbCoef the ucbCoef to set
     */
    public void setUcbCoef(double ucbCoef) {
        this.ucbCoef = ucbCoef;
    }

    /**
     * @return the deathWeight
     */
    public double getDeathWeight() {
        return guidedSimulator.getDeathWeight();
    }

    /**
     * @param deathWeight the deathWeight to set
     */
    public void setDeathWeight(double deathWeight) {
        guidedSimulator.setDeathWeight(deathWeight);
    }

    /**
     * @return the simulationDepth
     */
    public int getSimulationDepth() {
        return guidedSimulator.getMaxDepth();
    }

    /**
     * @param simulationDepth the simulationDepth to set
     */
    public void setSimulationDepth(int simulationDepth) {
        guidedSimulator.setMaxDepth(simulationDepth);
    }

    /**
     * @return the randomSimulationMoveProbability
     */
    public double getRandomSimulationMoveProbability() {
        return guidedSimulator.getRandomMoveProb();
    }

    /**
     * @param randomSimulationMoveProbability the randomSimulationMoveProbability to set
     */
    public void setRandomSimulationMoveProbability(double randomSimulationMoveProbability) {
        guidedSimulator.setRandomMoveProb(randomSimulationMoveProbability);
    }

    public T mcTree() {
        return mctree;
    }

    protected abstract void updateTree(Game timeDue);
    protected abstract M cloneMove(M move);

    @Override
    public M getMove(Game game, long timeDue) {
        if (timeDue==-1) {
            /* prevent infinite decisions */
            System.err.printf("Warning: timeDue not set\n");
            timeDue = System.currentTimeMillis()+DEFAULT_TIME_MILLIS;
        }

        /* initialize timing */
        long start_time = System.currentTimeMillis();
        int iteration_count = 0;

        /* update MC-tree */
        updateTree(game);

        /* do the iteration until time/iterations limit reached */
        do {
            if (!Double.isNaN(mcTree().iterate())) {
                iteration_count++;
                current_simulations++;
            }
        } while ((System.currentTimeMillis()+MILLIS_TO_FINISH)<timeDue);

        /* choose pacman's next move */
        M move = mctree.bestMove(game);

        /* update pacman's decision gap */
        if (mcTree().root().ticksToGo()==0) {
            pacman_decision_gap = 1;
        } else {
            pacman_decision_gap++;
        }

        /* check state validity */
        assert Utils.testRoot(game, mcTree())==null;

        /* print information about move */
        if (verbose) {
            double computation_time = (System.currentTimeMillis()-start_time)/1000.0;
            int pacman_pos = game.getPacmanCurrentNodeIndex();
            System.out.printf("MOVE INFO [node_index=%d[%d;%d],gap=%d]: iterations: %d, computation time: %.3f s, move: %s, tree size: %d\n",
                    pacman_pos, game.getNodeXCood(pacman_pos), game.getNodeYCood(pacman_pos),
                    pacman_decision_gap, iteration_count, computation_time, move, mcTree().size());

            /* print MC-tree if pacman (or ghosts) has to choose a move */
            if (mcTree().root().ticksToGo()==0) {
                System.out.printf("%s", mcTree().toString(2));
            }
        }

        /* return move */
        previous_game = game.copy();
        last_move = cloneMove(move);
        if (verbose&&timeDue - System.currentTimeMillis()<0) {
            System.err.printf("Missed turn, delay: %d ms\n", -(timeDue - System.currentTimeMillis()));
        }
        total_time_millis += System.currentTimeMillis()-start_time;
        total_simulations += iteration_count;
//        System.err.printf("sims: %s, millis: %s, sps: %s\n", totalSimulations(), totalTimeMillis(), simulationsPerSecond());

        if (mctree.decisionNeeded()) {
            decision_simulations.add(current_simulations);
            current_simulations = 0;
        }

        return move;
    }

    @Override public long totalTimeMillis() { return total_time_millis; }
    @Override public long totalSimulations() { return total_simulations; }
    @Override public double simulationsPerSecond() { return total_simulations/(0.001*total_time_millis); }


    @Override public double averageDecisionSimulations() {
        return total_simulations/(double)decision_simulations.size();
    }

    @Override public List<Long> decisionSimulations() { return decision_simulations; }
}
