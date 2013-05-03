package communication.messages;

import java.util.EnumMap;
import java.util.Map;
import mcts.GhostsTree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/* Message format:
 * Records (1-4) of:
 * 3 bits ghost move (Left, up, down, right, neutral)
 * 2 bits number (1-4) of ghost moves records
 * Set of ghost moves records
 * 3 bits align
 *
 * Ghost move records consist of:
 * Ghost moves (4x2 bits - for ghosts, four actions), if ghost is not at turn, value is interpreted as GHOST.NEUTRAL
 * Move value - 32bits (int) //XXX: some prefix code?
 *
 */
public class RootMessage extends Message {
    EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> valued_moves;

    public RootMessage(EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> valued_moves) {
        super("roots");
        this.valued_moves = valued_moves;
    }

    @Override
    public long length() {
        long bit_length = 0;

        for (Map<EnumMap<GHOST, MOVE>, Long> root: valued_moves.values()) {
            bit_length += 1 + root.size()*5;
        }
        return bit_length;
    }

    public EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> getRoots() {
        return valued_moves;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.name, valued_moves);
    }
}
