package mcts;

public interface UCBNode {
    public int visitCount();
    public double ucbValue();
}
