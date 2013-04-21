package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import pacman.game.Constants.GHOST;

public class SynchronizedDummyGhosts extends DistributedMCTSController {
    public SynchronizedDummyGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            DummyGhostAgent agent = new DummyGhostAgent(this, ghost);
            agent.setRandomSeed(seed);
            agent.setEqualRandomSeed(true);
            addGhostAgent(agent);
        }
    }
}
