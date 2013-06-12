package communication;

import java.util.EnumMap;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.entries.SynchronizedDummyGhosts;
import org.junit.Test;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.SimplifiedGame;
import static org.junit.Assert.*;
import test_utils.TestUtils;

public class DistributedMCTSVirtualTimerTest {
    @Test
    public void testTimer() {
        DistributedMCTSController controller = new SynchronizedDummyGhosts();
        Game game = new SimplifiedGame(0);
        long startTime = controller.currentVirtualMillis();

        for (int i=0; i<10; i++) {
            long now = controller.currentVirtualMillis();
            EnumMap<GHOST,MOVE> ghostMove = controller.getMove(game, System.currentTimeMillis()+20);
            assertEquals(5, controller.currentVirtualMillis()-now, 2);
            assertEquals(5*(i+1), controller.currentVirtualMillis()-startTime, 2*(i+1));
            game.advanceGame(MOVE.NEUTRAL, ghostMove);
        }

        TestUtils.sleep(200);
        assertEquals(50, controller.currentVirtualMillis()-startTime, 10);
    }
}
