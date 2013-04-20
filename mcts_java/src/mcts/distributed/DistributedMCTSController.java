package mcts.distributed;

import utils.VirtualTimer;
import mcts.distributed.agents.GhostAgent;
import communication.messages.Message;
import communication.Channel;
import communication.Network;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mcts.Constants;
import mcts.MCTSControllerStats;
import mcts.SimulationsStat;
import mcts.TreeSimulationsStat;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.VerboseLevel;

public class DistributedMCTSController
        extends Controller<EnumMap<GHOST,MOVE>>
        implements VirtualTimer, MCTSControllerStats {

    protected Network network = new Network();
    protected Map<GHOST, GhostAgent> agents = new EnumMap<GHOST, GhostAgent>(GHOST.class);
    protected int currentGhost = 0;
    protected EnumMap<GHOST,MOVE> moves = new EnumMap<GHOST,MOVE>(GHOST.class);
    protected VerboseLevel verboseLevel = VerboseLevel.QUIET;
    private long channelBufferSize = Constants.DEFAULT_CHANNEL_BUFFER_SIZE;

    private static GHOST ghosts[] = {GHOST.BLINKY, GHOST.PINKY, GHOST.INKY, GHOST.SUE};
    private long moveNumber = 0;

    protected long totalTimeMillis = 0;

    private long endTime;
    private long startTime;

    public DistributedMCTSController()  {
        this.network.setTimer(this);
    }

    public DistributedMCTSController addGhostAgent(GhostAgent ghostAgent) {
        assert !agents.containsKey(ghostAgent.ghost());
        for (GhostAgent ally: agents.values()) {
            Channel out_channel = network.openChannel(String.format("%s$%s", ghostAgent.ghostName(), ally.ghostName()), channelBufferSize);
            Channel in_channel = network.openChannel(String.format("%s$%s", ally.ghostName(), ghostAgent.ghostName()), channelBufferSize);
            ghostAgent.addAlly(out_channel, ally);
            ally.addAlly(in_channel, ghostAgent);
        }
        agents.put(ghostAgent.ghost(), ghostAgent);
        return this;
    }

    public long currentMillis() { return totalTimeMillis+(startTime>endTime? (System.currentTimeMillis()-startTime): 0); }
    @Override public long currentVirtualMillis() { return currentMillis()/agents.size(); }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        startTime = System.currentTimeMillis();
        assert agents.size()==4;
        moveNumber++;

        /* update agents' trees */
        for (GHOST ghost: GHOST.values()) {
            agents.get(ghost).truncateNetworkBuffers();
            agents.get(ghost).updateTree(game);
        }

        /* alternately run logic of agents (simulates parallel run) */
        do {
            agents.get(ghosts[currentGhost]).step();
            currentGhost = (currentGhost+1)%4;
        } while ((System.currentTimeMillis()+Constants.MILLIS_TO_FINISH)<timeDue);

        /* gather ghosts' moves and return result */
        for (GHOST ghost: GHOST.values()) {
            moves.put(ghost, agents.get(ghost).getMove());
        }
        if (verboseLevel.check(VerboseLevel.VERBOSE)) {
            int total_simulations_count = 0;

            for (GhostAgent agent: agents.values()) {
                total_simulations_count += agent.getTree().size();
            }
            double computation_time = (System.currentTimeMillis()-startTime)/1000.0;
            System.out.printf("MOVE INFO [node_index=%d]: computation time: %.3f s, simulations: %s, move (no.%s): %s\n",
                    game.getPacmanCurrentNodeIndex(), computation_time, total_simulations_count, moveNumber, moves);
            if (timeDue - System.currentTimeMillis()<0) {
                System.err.printf("Missed turn, delay: %d ms\n", -(timeDue - System.currentTimeMillis()));
            }
        }

        endTime = System.currentTimeMillis();
        totalTimeMillis += System.currentTimeMillis() - startTime;
        return moves;
    }

    @Override
    public long totalTimeMillis() {
        return totalTimeMillis;
    }

    @Override
    public long totalSimulations() {
       long simulations = 0;
        for (GhostAgent agent: agents.values()) {
            simulations += agent.totalSimulations();
        }
        return simulations;
    }

    @Override
    public double simulationsPerSecond() {
        return totalSimulations()/(0.001*totalTimeMillis());
    }

    @Override
    public void setUcbCoef(double ucbCoef) {
        for (GhostAgent agent: agents.values()) {
            agent.setUcbCoef(ucbCoef);
        }
    }

    @Override
    public void setDeathWeight(double deathWeight) {
        for (GhostAgent agent: agents.values()) {
            agent.setDeathWeight(deathWeight);
        }
    }

    @Override
    public void setSimulationDepth(int simulationDepth) {
        for (GhostAgent agent: agents.values()) {
            agent.setSimulationDepth(simulationDepth);
        }
    }

    @Override
    public void setRandomSimulationMoveProbability(double randomSimulationMoveProbability) {
        for (GhostAgent agent: agents.values()) {
            agent.setRandomSimulationMoveProbability(randomSimulationMoveProbability);
        }
    }

    @Override
    public double averageDecisionSimulations() {
        double sum = 0;
        for (GhostAgent agent: agents.values()) {
            sum += agent.averageDecisionSimulations();
        }

        return sum/agents.size();
    }


    public Network getNetwork() {
        return network;
    }

    public VerboseLevel getVerboseLevel() {
        return verboseLevel;
    }

    @Override public void setVerboseLevel(VerboseLevel verboseLevel) {
        this.verboseLevel = verboseLevel;
    }

    public double getUcbCoef() {
        return agents.get(GHOST.BLINKY).getUcbCoef();
    }

    public double getDeathWeight() {
        return agents.get(GHOST.BLINKY).getDeathWeight();
    }

    public int getSimulationDepth() {
        return agents.get(GHOST.BLINKY).getSimulationDepth();
    }

    public double getRandomSimulationMoveProbability() {
        return agents.get(GHOST.BLINKY).getRandomSimulationMoveProbability();
    }

    public double transmittedSuccessfullyPerSecond() {
        long transmittedSuccessfully = 0;
        for (Channel channel: network.getChannels().values()) {
            transmittedSuccessfully += channel.transmittedSuccessfully();
        }
        return 1000*transmittedSuccessfully/(4.0*totalTimeMillis());
    }

    public double transmittedTotalPerSecond() {
        long transmittedTotal = 0;
        for (Channel channel: network.getChannels().values()) {
            transmittedTotal += channel.transmittedTotal();
        }
        return 1000*transmittedTotal/(double)(network.getChannels().size()*(totalTimeMillis()/4.0));
    }
}
