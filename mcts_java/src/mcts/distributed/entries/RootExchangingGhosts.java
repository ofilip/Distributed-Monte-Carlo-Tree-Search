package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import mcts.distributed.agents.GhostAgent;
import mcts.distributed.agents.RootExchangingAgent;
import pacman.game.Constants.GHOST;
import utils.VerboseLevel;

public class RootExchangingGhosts extends DistributedMCTSController {
    private long calculatedSimulations = 0;
    private long receivedRootsSize = 0;

    public RootExchangingGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            RootExchangingAgent agent = new RootExchangingAgent(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }

    public double rootSizeRatio() {
        return receivedRootsSize/(double)(3*calculatedSimulations);
    }



    @Override
    public void calculateControllerSpecificStatistics() {
        if (!((RootExchangingAgent)agents.get(GHOST.BLINKY)).rootSendingActive()) return;

        for (GhostAgent ghostAgent: agents.values()) {
            RootExchangingAgent rootAgent = (RootExchangingAgent)ghostAgent;
            if (verboseLevel.check(VerboseLevel.VERBOSE)&&
                ghostAgent.ghost()==GHOST.BLINKY) {
                System.out.printf("[%s] sims: %s, roots: %s\n", rootAgent.ghost(), rootAgent.currentTreeSize(), rootAgent.currentReceivedRootsSize());
            }
            calculatedSimulations += rootAgent.currentTreeSize();
            receivedRootsSize += rootAgent.currentReceivedRootsSize();
        }
    }
}
