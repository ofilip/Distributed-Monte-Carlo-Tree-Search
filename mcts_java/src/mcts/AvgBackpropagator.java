package mcts;

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
        return backpropagateReceived(node, from, reward, count, false);
    }

    public long backpropagateReceived(MCNode node, GHOST from, double reward, int count, boolean previous_found) {
        int previously_received = node.getReceivedVisitCount(from);
        double previously_received_value = node.getReceivedValue(from);

        assert(!previous_found||previously_received==0);

        if (previously_received>0) {
            /* drop previously received simulations found on path */
            update(node, previously_received_value, -previously_received, true);
            node.received_visit_count.put(from, 0);
        }
//        int orig_count = node.visit_count;
//        double orig_value = node.value;
        update(node, reward, count, true);

        if (previously_received>0) {
            /* merge reward and count with values found in node.received_* */
            //count = node.visit_count - orig_count;
            //reward = (node.visit_count*node.value - orig_count*orig_value)/count;
            count = count - previously_received;
            if (count==0) return previously_received;
            reward = (count*reward - previously_received*previously_received_value)/count;
            assert(node.visit_count>=0);
            assert(!Double.isNaN(reward));
        }

        if (!node.isRoot()) {
            previously_received += backpropagateReceived(node.parent, from, reward, count, previous_found||previously_received>0);
        }

        return previously_received;
    }

    private AvgBackpropagator() {}

    public static AvgBackpropagator getInstance() {
        return instance;
    }
}
