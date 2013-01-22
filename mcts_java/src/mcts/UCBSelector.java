package mcts;

public class UCBSelector implements Selector {
    private int trial_threshold;
    private Simulator simulator;
    
    public UCBSelector(int trial_threshold, Simulator simulator) {
        assert trial_threshold>=0;
        this.trial_threshold = trial_threshold;
        this.simulator = trial_threshold>0? simulator: null;
    }
    
    private MCNode best(MCNode node) {            
        double best_val = Double.NEGATIVE_INFINITY;
        MCNode best = null;

        for (MCNode child: node.children()) {
            double curr_val = child.ucbValue();

            if (curr_val>best_val) {
                best_val = curr_val;
                best = child;
            }
        }

        return best;
    }
    
    @Override
    public MCNode select(MCNode node) {
        if (node.visitCount()<trial_threshold) {
            return simulator.nodeStep(node);
        }   
        
        return best(node);
    }
}