package mcts;

import java.util.*;
import pacman.game.Constants.*;

public abstract class Action {
    public enum Type {
        PACMAN(2),
        GHOST(8);

        private long bitLength;

        private Type(long bit_length) { this.bitLength = bit_length; }
        public Long bitLength() { return bitLength; }
    }

    private Type type;

    public Action(Type type) { this.type = type; }

    public Type type() { return type; }
    public abstract MOVE pacmanMove();
    public abstract EnumMap<GHOST, MOVE> ghostMove();

    @Override
    public String toString() {
        switch (type) {
            case PACMAN:
                return "P/"+pacmanMove();
            case GHOST:
                return "G/"+ghostMove();
            default:
                assert(false);
        }
        return null;
    }
}
