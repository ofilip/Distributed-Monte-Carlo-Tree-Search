package mcts;

import mcts.Utils;

public class AvgBackpropagator extends Backpropagator {
    private static AvgBackpropagator instance = new AvgBackpropagator();
    
    @Override
    protected void update(MCNode node, double reward) {
        node.value = Utils.addToAvg(node.value, node.visit_count, reward);
    }
    
    private AvgBackpropagator() {}
    
    public static AvgBackpropagator getInstance() {
        return instance;
    }
}
