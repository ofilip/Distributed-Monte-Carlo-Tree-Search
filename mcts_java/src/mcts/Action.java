package mcts;

import java.util.*;
import pacman.game.Constants.*;

public abstract class Action {
    public enum Type {
        PACMAN(2),
        GHOST(8);
        
        private long bit_length;
        
        private Type(long bit_length) { this.bit_length = bit_length; }
        public Long bitLength() { return bit_length; }
    }
    
    private Type type;
    
    public Action(Type type) { this.type = type; }
    
    public Type type() { return type; }
    public abstract MOVE pacmanMove();
    public abstract EnumMap<GHOST, MOVE> ghostMove();
}
