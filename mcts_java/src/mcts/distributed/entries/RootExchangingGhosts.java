package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import mcts.distributed.agents.GhostAgent;
import mcts.distributed.agents.RootExchangingAgent;
import pacman.game.Constants.GHOST;

public class RootExchangingGhosts extends DistributedMCTSController {
    public RootExchangingGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            RootExchangingAgent agent = new RootExchangingAgent(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }

    public double rootSizeRatio() {
        long rootsTransmitted = 0;
        long simulationCount = 0;
        for (GhostAgent agent: agents.values()) {
            RootExchangingAgent rootAgent = (RootExchangingAgent)agent;
            rootsTransmitted += rootAgent.currentReceivedRootsSize();
            simulationCount += rootAgent.rootSimulations();
        }
        return rootsTransmitted/(double)(3*simulationCount);
    }
}
