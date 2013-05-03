package communication.messages;

import java.util.EnumMap;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class MoveMessage extends Message {
    EnumMap<GHOST, MOVE> moves;

    public MoveMessage(EnumMap<GHOST, MOVE> moves) {
        super("moves");
        this.moves = moves.clone();
    }

    @Override
    public long length() {
        return 1; /* 4x2 bits for moves */
    }

    public EnumMap<GHOST, MOVE> moves() {
        return moves;
    }

    @Override
    public String toString() {
        return String.format("MOVE(%s)", moves);
    }
}
