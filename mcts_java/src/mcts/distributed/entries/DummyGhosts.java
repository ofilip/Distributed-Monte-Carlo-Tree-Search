package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import pacman.game.Constants.GHOST;

public class DummyGhosts extends DistributedMCTSController {
    public DummyGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            DummyGhostAgent agent = new DummyGhostAgent(this, ghost);
            agent.setRandomSeed(seed);
            addGhostAgent(agent);
        }
    }
}
