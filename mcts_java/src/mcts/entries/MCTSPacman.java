package mcts.entries;

import java.util.EnumMap;
import mcts.*;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.PlainMCTSController;
import mcts.MCTree;
import mcts.GuidedSimulator;
import mcts.PacmanTree;
import mcts.Selector;
import mcts.UCBSelector;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MCTSPacman extends PlainMCTSController<PacmanTree, MOVE> {


//    public MCTSPacman(int simulation_depth, double ucb_coef, boolean verbose, int iterations) {
//        super(simulation_depth, ucb_coef, verbose, iterations);
//    }

    @Override
    protected void updateTree(Game game) {
        if (mcTree()==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=currentLevel /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||prevousMove==MOVE.NEUTRAL /* last getMove() didn't finish in limit */
                ||prevousMove!=game.getPacmanLastMoveMade()
                ||mctree.root().getTotalTicks()>guidedSimulator.getMaxDepth()/2 /* simulation is too much shortened */
                ) {
            /* (re)initialize MC-tree and its components */
            mctree = new PacmanTree(game, ucbSelector, guidedSimulator, backpropagator, getUcbCoef());

            /* remember current level */
            currentLevel = game.getCurrentLevel();
        } else {
            assert previousGame!=null;
            EnumMap<Constants.GHOST, Constants.MOVE> last_ghosts_moves = Utils.lastGhostsDecisionMoves(game, previousGame);
            mcTree().advanceTree(game.getPacmanLastMoveMade(), last_ghosts_moves);
        }
        prevousMove = MOVE.NEUTRAL;
    }

    @Override
    protected MOVE cloneMove(MOVE move) {
        return move;
    }
}