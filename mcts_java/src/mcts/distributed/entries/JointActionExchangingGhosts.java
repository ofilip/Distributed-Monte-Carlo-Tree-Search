package mcts.distributed.entries;

import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.JointActionExchangingAgent;
import pacman.game.Constants.GHOST;

public class JointActionExchangingGhosts extends DistributedMCTSController {
    public JointActionExchangingGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            JointActionExchangingAgent agent = new JointActionExchangingAgent(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }
}
