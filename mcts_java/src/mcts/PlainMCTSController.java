package mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.game.Game;
import utils.VerboseLevel;

public abstract class PlainMCTSController<T extends MCTree<M>, M>
                        extends Controller<M>
                        implements MCTSControllerStats {
    protected T mctree = null;
    protected int currentLevel;
    protected Backpropagator backpropagator = AvgBackpropagator.getInstance();
    protected Random random = new Random(System.currentTimeMillis());
    protected GuidedSimulator guidedSimulator = new GuidedSimulator(random);
    protected UCBSelector ucbSelector = new UCBSelector(guidedSimulator);

    protected Game previousGame = null;
    protected int pacmanDecisionGap = 1;
    protected M prevousMove;
    protected long totalSimulations = 0;
    protected long totalTimeMillis = 0;
    protected long decisions = 0;

    private VerboseLevel verboseLevel = VerboseLevel.QUIET;
    private double ucbCoef = 0.3;


    public PlainMCTSController() {

    }

    @Override public VerboseLevel getVerboseLevel() { return verboseLevel; }
    @Override public void setVerboseLevel(VerboseLevel verboseLevel) { this.verboseLevel = verboseLevel; }

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
            timeDue = System.currentTimeMillis()+ Constants.DEFAULT_TIME_MILLIS;
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
            }
        } while ((System.currentTimeMillis()+Constants.MILLIS_TO_FINISH)<timeDue);

        /* choose pacman's next move */
        M move = mctree.bestMove(game);

        /* update pacman's decision gap */
        if (mcTree().root().ticksToGo()==0) {
            pacmanDecisionGap = 1;
        } else {
            pacmanDecisionGap++;
        }

        /* check state validity */
        assert Utils.testRoot(game, mcTree())==null;

        /* print information about move */
        if (verboseLevel.check(VerboseLevel.VERBOSE)) {
            double computation_time = (System.currentTimeMillis()-start_time)/1000.0;
            int pacman_pos = game.getPacmanCurrentNodeIndex();
            System.out.printf("MOVE INFO [node_index=%d[%d;%d],gap=%d]: iterations: %d, computation time: %.3f s, move: %s, tree size: %d\n",
                    pacman_pos, game.getNodeXCood(pacman_pos), game.getNodeYCood(pacman_pos),
                    pacmanDecisionGap, iteration_count, computation_time, move, mcTree().size());

            /* print MC-tree if pacman (or ghosts) has to choose a move */
            if (mcTree().root().ticksToGo()==0) {
                System.out.printf("%s", mcTree().toString(2));
            }
        }

        /* return move */
        previousGame = game.copy();
        prevousMove = cloneMove(move);
        if (verboseLevel.check(VerboseLevel.VERBOSE) &&timeDue - System.currentTimeMillis()<0) {
            System.err.printf("Missed turn, delay: %d ms\n", -(timeDue - System.currentTimeMillis()));
        }
        totalTimeMillis += System.currentTimeMillis()-start_time;
        totalSimulations += iteration_count;
//        System.err.printf("sims: %s, millis: %s, sps: %s\n", totalSimulations(), totalTimeMillis(), simulationsPerSecond());

        if (mctree.decisionNeeded()) {
            decisions++;
        }

        return move;
    }

    @Override public long totalTimeMillis() { return totalTimeMillis; }
    @Override public long totalSimulations() { return totalSimulations; }
    @Override public double simulationsPerSecond() { return totalSimulations/(0.001*totalTimeMillis); }


    @Override public double averageDecisionSimulations() {
        return totalSimulations/(double)decisions;
    }
}
