package mcts.entries.ghosts;

import java.util.EnumMap;
import mcts.Utils;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.GhostsTree;
import mcts.MCTSController;
import mcts.GuidedSimulator;
import mcts.PacmanTree;
import mcts.Selector;
import mcts.UCBSelector;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MCTSGhosts extends MCTSController<GhostsTree, EnumMap<GHOST, MOVE>> {
//    public MCTSGhosts() {
//        this(120, 0.3, false, GuidedSimulator.DEFAULT_RANDOM_MOVE_PROB, GuidedSimulator.DEFAULT_DEATH_WEIGHT);
//    }

//    public MCTSGhosts(int simulation_depth, double ucb_coef, boolean verbose) {
//        super(simulation_depth, ucb_coef, verbose);
//    }

//    public MCTSGhosts(int simulation_depth, double ucb_coef, boolean verbose, double random_simulation_move_probability, double death_weight) {
//        super(simulation_depth, ucb_coef, verbose, random_simulation_move_probability, death_weight);
//    }

    //TODO: remove iterations related code
//    private MCTSGhosts(int simulation_depth, double ucb_coef, boolean verbose, int iterations, double death_weight) {
//        super(simulation_depth, ucb_coef, verbose, iterations);
//    }

    private void initializeTree(Game game) {
        /* (re)initialize MC-tree and its components */
        mctree = new GhostsTree(game, ucbSelector, guidedSimulator, backpropagator, getUcbCoef());
    }

    @Override
    protected void updateTree(Game game) {
        if (mcTree()==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=currentLevel /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||last_move==null /* last getMove() didn't finish in limit */
                ||!Utils.compareGhostsMoves(last_move, Utils.lastGhostsMoves(game))
                ||mctree.root().getTotalTicks()>guidedSimulator.getMaxDepth()/2 /* simulation is too much shortened */
                ) {
            /* (re)initialize MC-tree and its components */
            initializeTree(game);

            /* remember current level */
            currentLevel = game.getCurrentLevel();
        } else {
            assert previous_game!=null;
            EnumMap<GHOST, MOVE> last_ghosts_moves = Utils.lastGhostsDecisionMoves(game, previous_game);

            if (mcTree().root().ticksToGo()==0) {
                initializeTree(game);
            } else {
                mcTree().advanceTree(game.getPacmanLastMoveMade(), last_ghosts_moves);
            }
        }
        last_move = null;
    }

    @Override
    protected EnumMap<GHOST, MOVE> cloneMove(EnumMap<GHOST, MOVE> move) {
        return move.clone();
    }
}