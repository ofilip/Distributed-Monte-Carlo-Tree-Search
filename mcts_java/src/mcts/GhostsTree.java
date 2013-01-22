package mcts;

import java.util.EnumMap;
import mcts.Utils;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostsTree extends MCTree<EnumMap<GHOST, MOVE>> {
    
    public GhostsTree(Game game, Selector selector, MySimulator simulator, Backpropagator backpropagator, double ucb1_coef) {
        super(game, selector, simulator, backpropagator, ucb1_coef);
        root = PacmanNode.createRoot(this, game);
    }
    
    @Override
    public EnumMap<GHOST, MOVE> bestMove(Game current_game) {
        MCNode node = root;
        
        /* skip ghosts decisions */
        while (node.pacmanOnTurn()&&node.ticks_to_go==0) {
            node = node.bestMove();
        }
        
        if (node.ghostsOnTurn()&&node.ticks_to_go==0) {
            return ((GhostsNode)node.bestMove()).ghostsMoves();
        } else {
            return Utils.ghostsFollowRoads(current_game);
        }
    }
}
