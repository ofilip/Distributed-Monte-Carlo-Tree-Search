package mcts.distributed.agents;

import java.util.EnumMap;
import mcts.AvgBackpropagator;
import mcts.GhostsTree;
import mcts.GuidedSimulator;
import mcts.MCTree;
import mcts.UCBSelector;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.VerboseLevel;

public abstract class FullMCTSGhostAgent extends GhostAgent {
    protected MOVE move = MOVE.NEUTRAL;
    protected GhostsTree mctree;
    protected Game current_game;
    protected int current_level;
    protected EnumMap<GHOST, MOVE> last_full_move;
    
    public FullMCTSGhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
    }
    
    private void initializeTree(Game game) {
        mctree = new GhostsTree(game, ucb_selector, my_simulator, backpropagator, ucb_coef);
    }
    
    @Override public MCTree getTree() { return mctree; }
    
    @Override public void updateTree(Game game) {
        if (mctree==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=current_level /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||last_full_move==null /* last getMove() didn't finish in limit */
                ||!Utils.compareGhostsMoves(last_full_move, Utils.lastGhostsMoves(game))
                ) {            
            /* (re)initialize MC-tree and its components */            
            initializeTree(game);
            
            /* remember current level */
            current_level = game.getCurrentLevel();
        } else {            
            assert current_game!=null;
            EnumMap<Constants.GHOST, MOVE> last_ghosts_moves = Utils.lastGhostsDecisionMoves(game, current_game);
            
            if (mctree.root().ticksToGo()==0) {
                initializeTree(game);
            } else {
                mctree.advanceTree(game.getPacmanLastMoveMade(), last_ghosts_moves);
            }
        }
        
        current_game = game.copy();
        last_full_move = null;
    }
}
