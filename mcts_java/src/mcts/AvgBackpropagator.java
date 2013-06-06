package mcts;

import mcts.Utils;
import pacman.game.Constants.GHOST;

public class AvgBackpropagator implements Backpropagator {
    private static AvgBackpropagator instance = new AvgBackpropagator();

    private void update(MCNode node, double reward, int count, boolean received) {
        if (node.visit_count+count>0) {
            node.value = Utils.addToAvg(node.value, node.visit_count, reward, count);
            if (!received) {
                node.calculated_value = Utils.addToAvg(node.calculated_value, node.calculated_visit_count, reward, count);
            }
        }
        node.visit_count += count;
        if (!received) {
            node.calculated_visit_count += count;
        }
    }

    @Override
    public void backpropagate(MCNode node, double reward, int count) {
        update(node, reward, count, false);

        if (!node.isRoot()) {
            backpropagate(node.parent, reward, count);
        }
    }

    public long backpropagateReceived(MCNode node, GHOST from, double reward, int count) {
        long previously_received = node.getReceivedVisitCount(from);

        if (previously_received>0) {
            /* drop previously received simulations found on path */
            update(node, node.getReceivedValue(from), -node.getReceivedVisitCount(from), true);
            node.received_visit_count.put(from, 0);
        }
        int orig_count = node.visit_count;
        double orig_value = node.value;
        update(node, reward, count, true);

        if (previously_received>0) {
            /* merge reward and count with values found in node.received_* */
            count = node.visit_count - orig_count;
            reward = (node.visit_count*node.value - orig_count*orig_value)/count;
        }

        if (!node.isRoot()) {
            previously_received += backpropagateReceived(node.parent, from, reward, count);
        }

        return previously_received;
    }

    private AvgBackpropagator() {}

    public static AvgBackpropagator getInstance() {
        return instance;
    }
}
