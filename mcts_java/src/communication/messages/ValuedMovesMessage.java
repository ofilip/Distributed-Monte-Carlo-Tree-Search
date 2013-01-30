package communication.messages;

import java.util.EnumMap;
import mcts.GhostsTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class ValuedMovesMessage extends Message {
    //Map<EnumMap<GHOST, MOVE>, Double> valued_moves;
    GhostsTree valued_moves_tree;
    
    public ValuedMovesMessage(GhostsTree valued_moves_tree) {
        super("valued_moves");
        this.valued_moves_tree = valued_moves_tree;
    }
    
    @Override
    public long length() {
        long length = 1;
        if (valued_moves_tree.root().pacmanOnTurn()) {
            
        }
//        return valued_moves.size
        //return 1; /* 4x2 bits for moves */
    }
    
    public EnumMap<GHOST, MOVE> move() {
        return move;
    }
}
