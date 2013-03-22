package mcts;

public interface IterationsCounter {
    public long totalTimeMillis();
    public long totalSimulations();
    public double simulationsPerSecond();
}
