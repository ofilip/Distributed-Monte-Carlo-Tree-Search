package mcts.distributed.entries;

import mcts.Constants;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.GhostAgent;
import mcts.distributed.agents.TreeCutExchangingAgent;
import pacman.game.Constants.GHOST;

public class TreeCutExchangingGhosts extends DistributedMCTSController {
    public TreeCutExchangingGhosts() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            TreeCutExchangingAgent agent = new TreeCutExchangingAgent(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }

    public void setCutsSentByTick(double cutsSent, long tickLength, double channelSpeed) {
        setCutByteSize((long)Math.floor(0.001*tickLength*channelSpeed/cutsSent));
    }

    public void setCutByteSize(long bytes) {
        for (GhostAgent agent: agents.values()) {
            TreeCutExchangingAgent treeCutAgent = (TreeCutExchangingAgent)agent;
            treeCutAgent.setCutByteSize(bytes);
        }
    }

    public long cutByteSize() {
        assert(agents.containsKey(GHOST.BLINKY));
        return ((TreeCutExchangingAgent)agents.get(GHOST.BLINKY)).getCutByteSize();
    }

    public double averageCutByteSize() {
        double size = 0;
        for (GhostAgent agent: agents.values()) {
            TreeCutExchangingAgent cutAgent = (TreeCutExchangingAgent)agent;
            size += cutAgent.averageCutSize();
        }
        return size/agents.size();
    }
}
