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

    public double averageSimulatonResultsMessageLength() {
        double averageLength = 0;
        for (GhostAgent agent: agents.values()) {
            SimulationResultsPassingAgent simulationPassingAgent = (SimulationResultsPassingAgent)agent;
            averageLength += simulationPassingAgent.averageSimulatonResultsMessageLength();
        }

        return averageLength/4;
    }

    public long totalReceivedSimulations() {
        long receivedSimulations = 0;
        for (GhostAgent agent: agents.values()) {
            SimulationResultsPassingAgent simulationPassingAgent = (SimulationResultsPassingAgent)agent;
            receivedSimulations += simulationPassingAgent.receivedSimulations();
        }
        return receivedSimulations;
    }

    public long totalCalculatedSimulations() {
        long calculatedSimulations = 0;
        for (GhostAgent agent: agents.values()) {
            SimulationResultsPassingAgent simulationPassingAgent = (SimulationResultsPassingAgent)agent;
            calculatedSimulations += simulationPassingAgent.calculatedSimulations();
        }
        return calculatedSimulations;
    }

    public double transmittedSimulationsRatio() {
        return totalReceivedSimulations()/(double)(3*totalCalculatedSimulations());
    }
}
