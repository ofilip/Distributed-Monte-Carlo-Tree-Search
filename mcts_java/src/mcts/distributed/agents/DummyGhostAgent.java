package mcts.distributed.agents;

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
        lastFullMove = mctree.bestMove(currentGame); /* in this context previous_game is current game */
        return lastFullMove.get(ghost);
    }

    @Override
    public long totalSimulations() {
        return totalSimulations;
    }
}
