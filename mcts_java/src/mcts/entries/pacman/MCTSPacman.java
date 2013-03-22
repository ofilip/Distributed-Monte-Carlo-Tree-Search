package mcts.entries.pacman;

import java.util.EnumMap;
import mcts.*;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.MCTSController;
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

public class MCTSPacman extends MCTSController<PacmanTree, MOVE> {
    public MCTSPacman(int simulation_depth, double ucb_coef, boolean verbose) {
        super(simulation_depth, ucb_coef, verbose);
    }

    public MCTSPacman(int simulation_depth, double ucb_coef, boolean verbose, int iterations) {
        super(simulation_depth, ucb_coef, verbose, iterations);
    }

    @Override
    protected void updateTree(Game game) {
        if (mcTree()==null /* new game or synchronization fail */
                ||game.getCurrentLevel()!=current_level /* new level */
                ||game.wasPacManEaten() /* pacman eaten */
                ||Utils.globalReversalHappened(game) /* accidental reversal */
                ||last_move==MOVE.NEUTRAL /* last getMove() didn't finish in limit */
                ||last_move!=game.getPacmanLastMoveMade()
                ) {
            /* (re)initialize MC-tree and its components */
            mctree = new PacmanTree(game, ucb_selector, my_simulator, backpropagator, ucb_coef);

            /* remember current level */
            current_level = game.getCurrentLevel();
        } else {
            assert previous_game!=null;
            EnumMap<Constants.GHOST, Constants.MOVE> last_ghosts_moves = Utils.lastGhostsDecisionMoves(game, previous_game);
            mcTree().advanceTree(game.getPacmanLastMoveMade(), last_ghosts_moves);
        }
        last_move = MOVE.NEUTRAL;
    }

    @Override
    protected MOVE cloneMove(MOVE move) {
        return move;
    }
}