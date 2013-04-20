package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.GhostAgent;
import mcts.distributed.agents.JointActionExchangingAgent;
import mcts.distributed.agents.SimulationResultsPassingAgent;
import pacman.game.Constants.GHOST;

public class SimulationResultsPassingGhosts extends DistributedMCTSController {
    public SimulationResultsPassingGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            GhostAgent agent = new SimulationResultsPassingAgent(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }
}
