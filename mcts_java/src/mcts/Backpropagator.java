package mcts;

public interface Backpropagator {
    public void backpropagate(MCNode node, double reward, int count);
    public void backpropagateReceived(MCNode node, double reward, int count);
}
