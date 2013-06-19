package mcts.distributed.entries;

import mcts.Constants;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.agents.GhostAgent;
import mcts.distributed.agents.TreeCutExchangingAgent2;
import pacman.game.Constants.GHOST;

public class TreeCutExchangingGhosts2 extends DistributedMCTSController {
    public TreeCutExchangingGhosts2() {
        long seed = System.currentTimeMillis();

        for (GHOST ghost: GHOST.values()) {
            TreeCutExchangingAgent2 agent = new TreeCutExchangingAgent2(this, ghost);
            agent.setRandomSeed(seed+ghost.ordinal());
            addGhostAgent(agent);
        }
    }

    public void setCutsSentByTick(double cutsSent, long tickLength, double channelSpeed) {
        setCutByteSize((long)Math.floor(0.001*tickLength*channelSpeed/cutsSent));
    }

    public void setCutByteSize(long bytes) {
        for (GhostAgent agent: agents.values()) {
            TreeCutExchangingAgent2 treeCutAgent = (TreeCutExchangingAgent2)agent;
            treeCutAgent.setCutByteSize(bytes);
        }
    }

    public long cutByteSize() {
        assert(agents.containsKey(GHOST.BLINKY));
        return ((TreeCutExchangingAgent2)agents.get(GHOST.BLINKY)).getCutByteSize();
    }
}
