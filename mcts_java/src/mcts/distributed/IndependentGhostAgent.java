package mcts.distributed;

import communication.DummyMessage;
import java.util.EnumMap;
import mcts.AvgBackpropagator;
import mcts.GhostsTree;
import mcts.MySimulator;
import mcts.UCBSelector;
import mcts.Utils;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class IndependentGhostAgent extends GhostAgent<DummyMessage> {
    protected MOVE move = MOVE.NEUTRAL;
    protected GhostsTree mctree;
    protected Game current_game;
    protected int current_level;
    protected EnumMap<GHOST, MOVE> last_full_move;
    
    public IndependentGhostAgent(GHOST ghost, int simulation_depth, double ucb_coef) {
        super(ghost, simulation_depth, ucb_coef, false);
    }
    
    private void initializeTree(Game game) {
        mctree = new GhostsTree(game, ucb_selector, my_simulator, backpropagator, ucb_coef);
    }
    
    @Override
    public void updateTree(Game game) {
        if (mctree==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=current_level /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
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
    }

    @Override
    public void step() {
        mctree.iterate();
    }

    @Override
    public MOVE getMove() {
        last_full_move = mctree.bestMove(current_game); /* in this context previous_game is current game */
        return last_full_move.get(ghost);
    }

}
