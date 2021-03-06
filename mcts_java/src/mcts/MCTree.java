package mcts;

import java.util.EnumMap;
import java.util.List;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public abstract class MCTree<M> {
    Selector selector;
    Selector best_move_selector = new UCBSelector(null, 0);
    GuidedSimulator simulator;
    Backpropagator backpropagator;
    double ucb1_coef;
    MCNode root;
    boolean optimisticTurns = true;

    public MCNode root() {
        return root;
    }

    public double iterate() { return iterate(null); }

    public double iterate(List<Action> action_list) {
        MCNode node = action_list==null? root.select(): root.select(action_list);
        if (node.isRoot()||!node.parent().game.wasPacManEaten()) {
            node.expand();
            double reward = node.simulate();
            node.backpropagate(reward);
            return reward;
        } else {
            /* do not extend subtree if pacman was eaten */
            node.terminal = true;
            node.backpropagate(node.value);
            return Double.NaN;
        }
    }

    private MCNode getNode(List<Action> action_list) throws InvalidActionListException {
        MCNode node = root;
        for (Action action: action_list) {
            node.expand();
            node = node.child(action);
            if (node==null) {
                throw new InvalidActionListException();
            }
        }
        node.expand();
        return node;
    }

    public void applySimulationResult(List<Action> action_list, double simulation_result) throws InvalidActionListException {
        MCNode node = getNode(action_list);
        node.backpropagate(simulation_result);
    }

    /** @return Number of previously received simulations masked by this node */
    public long applyTreeNode(GHOST from, List<Action> action_list, double simulation_result, int visit_count) throws InvalidActionListException {
        MCNode node = getNode(action_list);
        long res = node.backpropagateReceived(from, simulation_result, visit_count);
        node.received_value.put(from, simulation_result);
        node.received_visit_count.put(from, visit_count);
        return res;
    }

    public boolean getOptimisticTurns() { return optimisticTurns; }
    public void setOptimisticTurns(boolean optimisticTurns) { this.optimisticTurns = optimisticTurns; }

//    protected MCTree(MCTree tree, long depth) {
//        this.selector = tree.selector;
//        this.simulator = tree.simulator;
//        this.backpropagator = tree.backpropagator;
//        this.ucb1_coef = tree.ucb1_coef;
//        this.root = tree.root.copy(this, null, depth);
//    }

    public MCTree(Game game, Selector selector, GuidedSimulator simulator, Backpropagator backpropagator, double ucb1_coef) {
        this.selector = selector;
        this.simulator = simulator;
        this.backpropagator = backpropagator;
        this.ucb1_coef = ucb1_coef;
    }

    public abstract M bestMove(Game game);
    public abstract M bestDecisionMove();
    public abstract boolean decisionNeeded();

    public void moveToNode(MCNode node) {
        assert node.parent==root;
        node.parent = null;
        root = node;
    }

    public int advanceTree(MOVE last_pacman_move, EnumMap<GHOST, MOVE> last_ghosts_moves) {
        root.ticksToGo--;
        assert root.ticksToGo>=-2;
        int steps = 0;

        while (root.ticksToGo==-1) {
            MCNode next_node;
            if (root.pacmanOnTurn()) {
                next_node = root.child(last_pacman_move);
            } else {
                next_node = root.child(last_ghosts_moves);
            }
            next_node.expand();
            root = next_node;
            root.ticksToGo += -1; /* propagate -1 delay */
            root.parent = null; /* drop unreachable paths */
            steps++;
        }
        return steps;
    }

    public boolean isPacmanTree() {
        //return root.isGhostsNode();
        return this instanceof PacmanTree;
    }

    public boolean isGhostsTree() {
        return !isPacmanTree();
    }

    public String toString(int depth_limit) {
        return root.toString(depth_limit);
    }

    public int size() {
        return root.visitCount();
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
