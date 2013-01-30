package pacman.entries.ghosts;

import java.util.EnumMap;
import mcts.Utils;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.GhostsTree;
import mcts.MCTSController;
import mcts.MySimulator;
import mcts.PacmanTree;
import mcts.Selector;
import mcts.UCBSelector;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MCTSGhosts extends MCTSController<GhostsTree, EnumMap<GHOST, MOVE>> { 
    public MCTSGhosts(int simulation_depth, double ucb_coef, boolean verbose) {
        super(simulation_depth, ucb_coef, verbose);
    }      
    
    public MCTSGhosts(int simulation_depth, double ucb_coef, boolean verbose, int iterations) {
        super(simulation_depth, ucb_coef, verbose, iterations);
    }    
    
    private void initializeTree(Game game) {
        /* (re)initialize MC-tree and its components */            
        mctree = new GhostsTree(game, ucb_selector, my_simulator, backpropagator, ucb_coef);
    }
    
    @Override
    protected void updateTree(Game game) {
        if (mcTree()==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=current_level /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||last_move==null /* last getMove() didn't finish in limit */
                ||!Utils.compareGhostsMoves(last_move, Utils.lastGhostsMoves(game))
                ) {            
            /* (re)initialize MC-tree and its components */            
            initializeTree(game);
            
            /* remember current level */
            current_level = game.getCurrentLevel();
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