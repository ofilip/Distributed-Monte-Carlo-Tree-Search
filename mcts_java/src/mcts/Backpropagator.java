package mcts;

abstract public class Backpropagator {
    abstract protected void update(MCNode node, double reward);
    public void backpropagate(MCNode node, double reward) {
        update(node, reward);
        node.visit_count++;
        if (!node.isRoot()) {
            backpropagate(node.parent, reward);
        }
    }
}
