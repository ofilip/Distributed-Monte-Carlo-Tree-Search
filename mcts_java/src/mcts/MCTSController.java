package mcts;

import java.util.EnumMap;
import mcts.Utils;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

public abstract class MCTSController<T extends MCTree<M>, M> extends Controller<M> {
    protected T mctree = null;
    protected int current_level;
    protected Selector ucb_selector;
    protected Backpropagator backpropagator;
    protected GuidedSimulator my_simulator;
    protected int iterations;
    protected boolean verbose;
    protected double ucb_coef;
    protected Game previous_game = null;
    protected int pacman_decision_gap = 1;
    protected static final long MILLIS_TO_FINISH = 10;
    protected M last_move;

    public static final int DEFAULT_ITERATION_COUNT = 30;

    public MCTSController(int simulation_depth, double ucb_coef, boolean verbose) {
        this(simulation_depth, ucb_coef, verbose, DEFAULT_ITERATION_COUNT, -1);
    }

    public MCTSController(int simulation_depth, double ucb_coef, boolean verbose, double random_simulation_move_probability) {
        this(simulation_depth, ucb_coef, verbose, DEFAULT_ITERATION_COUNT, random_simulation_move_probability);
    }

    //TODO: remove the constructor, iterations variable and related code
    private MCTSController(int simulation_depth, double ucb_coef, boolean verbose, int iterations, double random_simulation_move_probability) {
        if (random_simulation_move_probability<-0.5) {
            this.my_simulator = new GuidedSimulator(simulation_depth, System.currentTimeMillis());
        } else {
            this.my_simulator = new GuidedSimulator(simulation_depth, System.currentTimeMillis(), random_simulation_move_probability);
        }
        this.ucb_selector = new UCBSelector(30, my_simulator);
        this.backpropagator = AvgBackpropagator.getInstance();
        this.iterations = iterations;
        this.ucb_coef = ucb_coef;
        this.verbose = verbose;
    }

    public T mcTree() {
        return mctree;
    }

    protected abstract void updateTree(Game timeDue);
    protected abstract M cloneMove(M move);

    public M getMove(Game game, long timeDue) {
        /* initialize timing */
        long start_time = System.currentTimeMillis();
        int iteration_count = 0;

        /* update MC-tree */
        updateTree(game);

        /* do the iteration until time/iterations limit reached */
        do {
            mcTree().iterate();
        } while (++iteration_count<iterations||(System.currentTimeMillis()+MILLIS_TO_FINISH)<timeDue);

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
            System.out.printf("MOVE INFO [node_index=%d,gap=%d]: iterations: %d, computation time: %.3f s, move: %s, tree size: %d\n",
                    game.getPacmanCurrentNodeIndex(), pacman_decision_gap, iteration_count, computation_time, move, mcTree().size());

            /* print MC-tree if pacman (or ghosts) has to choose a move */
            if (mcTree().root().ticksToGo()==0) {
                System.out.printf("%s", mcTree().toString(2));
                int i = 0;
            }
        }

        /* return move */
        previous_game = game.copy();
        last_move = cloneMove(move);
        if (verbose&&timeDue - System.currentTimeMillis()<0) {
            System.err.printf("Missed turn, delay: %d ms\n", -(timeDue - System.currentTimeMillis()));
        }
        return move;
    }
}
