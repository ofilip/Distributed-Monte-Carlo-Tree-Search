package mcts;

public interface SimulationsStat extends SimulationsCounter {
    public long totalTimeMillis();
    @Override public long totalSimulations();
    public double simulationsPerSecond();
}
