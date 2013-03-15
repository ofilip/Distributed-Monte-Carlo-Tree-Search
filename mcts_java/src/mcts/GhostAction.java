package mcts;

import java.util.EnumMap;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class GhostAction extends Action {    
    private EnumMap<GHOST,MOVE> ghost_move;
    
    public GhostAction(EnumMap<GHOST,MOVE> ghost_move) {
        super(Type.GHOST);
        this.ghost_move = ghost_move;
    }
    
    @Override public MOVE pacmanMove() { return null; }
    @Override public EnumMap<GHOST, MOVE> ghostMove() { return ghost_move; }

}
