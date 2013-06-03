package mcts;

import mcts.Utils;

public class AvgBackpropagator implements Backpropagator {
    private static AvgBackpropagator instance = new AvgBackpropagator();

    private void update(MCNode node, double reward, int count) {
        if (node.visit_count+count>0) {
            node.value = Utils.addToAvg(node.value, node.visit_count, reward, count);
        }
        node.visit_count += count;
    }

    @Override
    public void backpropagate(MCNode node, double reward, int count) {
        update(node, reward, count);

        if (!node.isRoot()) {
            backpropagate(node.parent, reward, count);
        }
    }

    public void backpropagateReceived(MCNode node, double reward, int count) {
        boolean previously_received = node.received_visit_count>0;
        if (previously_received) {
            /* drop previously received simulations found on path */
            update(node, node.received_value, -node.received_visit_count);
            node.received_visit_count = 0;
        }
        int orig_count = node.visit_count;
        double orig_value = node.value;
        update(node, reward, count);

        if (previously_received) {
            /* merge reward and count with values found in node.received_* */
            count = node.visit_count - orig_count;
            reward = (node.visit_count*node.value - orig_count*orig_value)/count;
        }

        if (!node.isRoot()) {
            backpropagateReceived(node.parent, reward, count);
        }
    }

    private AvgBackpropagator() {}

    public static AvgBackpropagator getInstance() {
        return instance;
    }
}
