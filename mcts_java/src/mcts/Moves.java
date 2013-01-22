package mcts;

import java.util.EnumMap;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class Moves {
    public MOVE pacmans;
    public EnumMap<GHOST, MOVE> ghosts;
    
    public Moves(MOVE pacmans, EnumMap<GHOST, MOVE> ghosts) {
        this.pacmans = pacmans;
        this.ghosts = ghosts;
    }
}
