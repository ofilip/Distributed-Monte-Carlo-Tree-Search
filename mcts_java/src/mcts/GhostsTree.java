package mcts;

import java.util.EnumMap;
import mcts.Utils;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostsTree extends MCTree<EnumMap<GHOST, MOVE>> implements Cloneable {
    public GhostsTree copy(long depth)  {
        return new GhostsTree(this, depth);
    }

    public GhostsTree copyGhostsMoves() {
        if (this.root.pacmanOnTurn()) {
            /* if pacman is on turn then joint turn happens so it is necessary
             * to return the tree containing two levels. */
            return copy(2);
        } else {
            /* ghost is on turn => return first level only */
            return copy(1);
        }
    }

    public GhostsTree(GhostsTree tree, long depth) {
        super(tree, depth);
    }

    public GhostsTree(Game game, Selector selector, GuidedSimulator simulator, Backpropagator backpropagator, double ucb1_coef) {
        super(game, selector, simulator, backpropagator, ucb1_coef);
        root = PacmanNode.createRoot(this, game);
    }

    @Override
    public EnumMap<GHOST, MOVE> bestMove(Game current_game) {
        MCNode node = root;

        /* skip pacman decisions */
        while (node.pacmanOnTurn()&&node.ticks_to_go==0) {
            node = node.bestMove();
        }

        if (node.ghostsOnTurn()&&node.ticks_to_go==0) {
            return ((GhostsNode)node.bestMove()).ghostsMoves();
        } else {
            return Utils.ghostsFollowRoads(current_game);
        }
    }

    @Override
    public boolean decisionNeeded() {
         MCNode node = root;

        /* skip pacman decisions */
        while (node.pacmanOnTurn()&&node.ticks_to_go==0) {
            node = node.bestMove();
        }

        return node.ghostsOnTurn()&&node.ticks_to_go==0;
    }
}
