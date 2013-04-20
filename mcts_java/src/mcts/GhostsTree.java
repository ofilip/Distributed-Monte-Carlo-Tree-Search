package mcts;

import java.util.EnumMap;
import mcts.Utils;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostsTree extends MCTree<EnumMap<GHOST, MOVE>> implements Cloneable {

    public GhostsTree(Game game, Selector selector, GuidedSimulator simulator, Backpropagator backpropagator, double ucbCoef) {
        super(game, selector, simulator, backpropagator, ucbCoef);
        root = PacmanNode.createRoot(this, game);
    }

    @Override
    public EnumMap<GHOST, MOVE> bestMove(Game currentGame) {
        MCNode node = root;

        /* skip pacman decisions */
        while (node!=null&&node.pacmanOnTurn()&&node.ticksToGo==0) {
            node = node.bestMove();
        }

        if (node==null) {
            return Utils.NEUTRAL_GHOSTS_MOVES;
        } else if (node.ghostsOnTurn()&&node.ticksToGo==0) {
            return ((GhostsNode)node.bestMove()).ghostsMoves();
        } else {
            return Utils.ghostsFollowRoads(currentGame);
        }
    }

    @Override
    public EnumMap<GHOST, MOVE> bestDecisionMove() {
        MCNode node = root;

        /* skip pacman decisions */
        while (node!=null&&node.pacmanOnTurn()) {
            node = node.bestMove();
        }

        return (node==null||node.bestMove()==null)? Utils.NEUTRAL_GHOSTS_MOVES: ((GhostsNode)node.bestMove()).ghostsMoves();
    }

    @Override
    public boolean decisionNeeded() {
         MCNode node = root;

        /* skip pacman decisions */
        while (node.pacmanOnTurn()&&node.ticksToGo==0) {
            node = node.bestMove();
        }

        return node.ghostsOnTurn()&&node.ticksToGo==0;
    }
}
