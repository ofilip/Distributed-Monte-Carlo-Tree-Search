package mcts;

import java.util.List;


public interface TreeSimulationsStat {
    public double averageDecisionSimulations();
    public List<Long> decisionSimulations();
}
