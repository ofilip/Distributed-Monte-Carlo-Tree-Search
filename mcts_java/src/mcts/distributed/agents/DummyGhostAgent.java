package mcts.distributed.agents;

import java.util.EnumMap;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class DummyGhostAgent extends FullMCTSGhostAgent {
    public long totalSimulations = 0;

    public DummyGhostAgent(DistributedMCTSController controller, GHOST ghost) {
        super(controller, ghost);
    }

    @Override
    public void step() {
        if (!Double.isNaN(mctree.iterate())) {
            totalSimulations++;
        }
    }

    @Override
    public MOVE getMove() {
        if (verboseLevel.check(VerboseLevel.DEBUGGING)&&Utils.ghostsNeedAction(currentGame)) {
            System.out.print(mctree.toString(2));
            System.out.printf("%s's full move: %s\n", ghost, mctree.bestMove(currentGame));
        }
        lastFullMove = mctree.bestMove(currentGame);
        return lastFullMove.get(ghost);
    }

    @Override
    public long totalSimulations() {
        return totalSimulations;
    }

    @Override
    public long calculatedSimulations() {
        return totalSimulations;
    }
}
