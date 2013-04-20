package mcts;

import java.io.IOException;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcts.Utils;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class PacmanTree extends MCTree<MOVE> {
    public PacmanTree(Game game, Selector selector, GuidedSimulator simulator, Backpropagator backpropagator, double ucb1_coef) {
        super(game, selector, simulator, backpropagator, ucb1_coef);
        root = GhostsNode.createRoot(this, game);
    }



    @Override
    public MOVE bestMove(Game currentGame) {
        MCNode node = root;

        /* skip ghosts decisions */
        while (node!=null&&node.ghostsOnTurn()&&node.ticksToGo==0) {
            node = node.bestMove();
        }

        if (node==null) {
            return MOVE.NEUTRAL;
        } else if (node.ghostsOnTurn()&&node.ticksToGo==0) {
            return ((PacmanNode)node.bestMove()).pacmanMove();
        } else {
            return Utils.pacmanFollowRoad(currentGame);
        }
    }

    @Override
    public MOVE bestDecisionMove() {
                MCNode node = root;

        /* skip ghosts decisions */
        while (node!=null&&node.ghostsOnTurn()) {
            node = node.bestMove();
        }

        return node==null? MOVE.NEUTRAL: ((PacmanNode)node.bestMove()).pacmanMove();
    }

    @Override
    public boolean decisionNeeded() {
        MCNode node = root;

        /* skip ghosts decisions */
        while (node.ghostsOnTurn()&&node.ticksToGo==0) {
            node = node.bestMove();
        }

        return node.pacmanOnTurn()&&node.ticksToGo==0;
    }
}
