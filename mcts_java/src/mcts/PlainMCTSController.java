package mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pacman.controllers.Controller;
import pacman.game.Game;
import utils.VerboseLevel;

public abstract class PlainMCTSController<T extends MCTree<M>, M>
                        extends Controller<M>
                        implements MCTSController {
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
    protected boolean optimisticTurns = true;

    private VerboseLevel verboseLevel = VerboseLevel.QUIET;
    private double ucbCoef = 0.3;

    @Override public boolean getOptimisticTurns() { return optimisticTurns; }
    @Override public void setOptimisticTurns(boolean optimisticTurns) { this.optimisticTurns = optimisticTurns; }

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
        long startTime = System.currentTimeMillis();
        int iterationCount = 0;

        /* update MC-tree */
        updateTree(game);

        /* do the iteration until time/iterations limit reached */
        do {
            if (!Double.isNaN(mcTree().iterate())) {
                iterationCount++;
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
        //assert Utils.testRoot(game, mcTree())==null;
//        if (Utils.testRoot(game, mcTree())!=null) {
//            int i=0;
//        }

        /* print information about move */
        if (verboseLevel.check(VerboseLevel.VERBOSE)) {
            double computationTime = (System.currentTimeMillis()-startTime)/1000.0;
            int pacman_pos = game.getPacmanCurrentNodeIndex();
            System.out.printf("MOVE INFO [node_index=%d[%d;%d],gap=%d]: iterations: %d, computation time: %.3f s, move: %s, tree size: %d\n",
                    pacman_pos, game.getNodeXCood(pacman_pos), game.getNodeYCood(pacman_pos),
                    pacmanDecisionGap, iterationCount, computationTime, move, mcTree().size());

            /* print MC-tree if pacman (or ghosts) has to choose a move */
            if (mcTree().root().ticksToGo()==0) {
                System.out.printf("%s", mcTree().toString(2));
            }
        }

        /* return move */
        previousGame = game.copy();
        prevousMove = cloneMove(move);
        totalTimeMillis += System.currentTimeMillis()-startTime;
        totalSimulations += iterationCount;

        if (mctree.decisionNeeded()) {
            decisions++;
        }

        return move;
    }

    @Override public long totalTimeMillis() { return totalTimeMillis; }
    @Override public double millisPerMove() { return totalTimeMillis()/(double)mctree.root.game.getTotalTime(); }
    @Override public long totalSimulations() { return totalSimulations; }
    @Override public double simulationsPerSecond() { return totalSimulations/(0.001*totalTimeMillis); }


    @Override public double averageDecisionSimulations() {
        return totalSimulations/(double)decisions;
    }
}
