package mcts;

public interface SimulationsStat extends SimulationsCounter {
    public long totalTimeMillis();
    public double millisPerMove();
    @Override public long totalSimulations();
    public double simulationsPerSecond();
}
