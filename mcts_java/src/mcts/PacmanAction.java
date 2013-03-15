package mcts;

import java.util.EnumMap;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class PacmanAction extends Action {
    private MOVE pacman_move;
    
    public PacmanAction(MOVE pacman_move) {
        super(Type.PACMAN);
        this.pacman_move = pacman_move;
    }
    
    @Override public MOVE pacmanMove() { return pacman_move; }
    @Override public EnumMap<GHOST, MOVE> ghostMove() { return null; }

}
