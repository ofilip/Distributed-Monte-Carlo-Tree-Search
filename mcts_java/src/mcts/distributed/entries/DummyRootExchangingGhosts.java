package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.DummyGhostAgent;
import mcts.distributed.agents.GhostAgent;
import mcts.distributed.agents.DummyRootExchangingAgent;
import pacman.game.Constants.GHOST;
import utils.VerboseLevel;

public class DummyRootExchangingGhosts extends DistributedMCTSController {
    private long calculatedSimulations = 0;
    private long receivedRootsSize = 0;

    public DummyRootExchangingGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            DummyRootExchangingAgent agent = new DummyRootExchangingAgent(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }

    public double rootSizeRatio() {
        return receivedRootsSize/(double)(3*calculatedSimulations);
    }



//    @Override
//    public void calculateControllerSpecificStatistics() {
//        if (!((DummyRootExchangingAgent)agents.get(GHOST.BLINKY)).rootSendingActive()) return;
//
//        for (GhostAgent ghostAgent: agents.values()) {
//            DummyRootExchangingAgent rootAgent = (DummyRootExchangingAgent)ghostAgent;
//            if (verboseLevel.check(VerboseLevel.VERBOSE)&&
//                ghostAgent.ghost()==GHOST.BLINKY) {
//                System.out.printf("[%s] sims: %s, roots: %s\n", rootAgent.ghost(), rootAgent.currentTreeSize(), rootAgent.currentReceivedRootsSize());
//            }
//            calculatedSimulations += rootAgent.currentTreeSize();
//            receivedRootsSize += rootAgent.currentReceivedRootsSize();
//        }
//    }
}
