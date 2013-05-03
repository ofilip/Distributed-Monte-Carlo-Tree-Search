package bug_coverage;


import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mcts.distributed.agents.FullMCTSGhostAgent;
import org.junit.Test;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;
import static org.junit.Assert.*;

final class MoveStrengthEntry implements Map.Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>> {
    private final EnumMap<GHOST,MOVE> key;
    private Pair<Integer,GHOST> value;

    public MoveStrengthEntry(EnumMap<GHOST,MOVE> key, Pair<Integer,GHOST> value) {
        this.key = key;
        this.value = value;
    }

    @Override public EnumMap<GHOST,MOVE> getKey() { return key; }
    @Override public Pair<Integer,GHOST> getValue() { return value; }
    @Override public Pair<Integer,GHOST> setValue(Pair<Integer,GHOST> value) { Pair<Integer,GHOST> old = this.value; this.value = value; return old; }
}

public class BugCoverage {
    @Test
    public void moveStrengthEntryComparatorTest() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field comparator = FullMCTSGhostAgent.class.getDeclaredField("moveStrengthEntryComparator");
        comparator.setAccessible(true);
        Comparator<Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>> cmp = (Comparator<Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>>) comparator.get(null);
        EnumMap<GHOST,MOVE> move1 = new EnumMap<GHOST,MOVE>(GHOST.class);
        EnumMap<GHOST,MOVE> move2 = new EnumMap<GHOST,MOVE>(GHOST.class);
        EnumMap<GHOST,MOVE> move3 = new EnumMap<GHOST,MOVE>(GHOST.class);
        EnumMap<GHOST,MOVE> move4 = new EnumMap<GHOST,MOVE>(GHOST.class);
        for (GHOST ghost: GHOST.values()) {
            move1.put(ghost, MOVE.LEFT);
            move2.put(ghost, MOVE.UP);
            move3.put(ghost, MOVE.RIGHT);
            move4.put(ghost, MOVE.DOWN);
        }
        Pair<Integer,GHOST> pair1 = new Pair<Integer,GHOST>(1, GHOST.BLINKY);
        Pair<Integer,GHOST> pair2 = new Pair<Integer,GHOST>(2, GHOST.BLINKY);
        Pair<Integer,GHOST> pair3 = new Pair<Integer,GHOST>(1, GHOST.PINKY);
        Pair<Integer,GHOST> pair4 = new Pair<Integer,GHOST>(3, GHOST.INKY);

        MoveStrengthEntry entry1 = new MoveStrengthEntry(move1, pair1);
        MoveStrengthEntry entry2 = new MoveStrengthEntry(move2, pair2);
        MoveStrengthEntry entry3 = new MoveStrengthEntry(move3, pair3);
        MoveStrengthEntry entry4 = new MoveStrengthEntry(move4, pair4);

        assertEquals(0, cmp.compare(entry1, entry1));
        assertEquals(-1, cmp.compare(entry1, entry2));
        assertEquals(1, cmp.compare(entry1, entry3));
        assertEquals(-1, cmp.compare(entry1, entry4));

        Map<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>> map = new HashMap<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>();
        map.put(move1, pair1);
        assertEquals(move1, Collections.max(map.entrySet(), cmp).getKey());
        assertEquals(move1, Collections.min(map.entrySet(), cmp).getKey());
        map.put(move2, pair2);
        assertEquals(move2, Collections.max(map.entrySet(), cmp).getKey());
        assertEquals(move1, Collections.min(map.entrySet(), cmp).getKey());
        map.put(move3, pair3);
        assertEquals(move2, Collections.max(map.entrySet(), cmp).getKey());
        assertEquals(move3, Collections.min(map.entrySet(), cmp).getKey());
        map.put(move4, pair4);
        assertEquals(move4, Collections.max(map.entrySet(), cmp).getKey());
        assertEquals(move3, Collections.min(map.entrySet(), cmp).getKey());
    }
}
