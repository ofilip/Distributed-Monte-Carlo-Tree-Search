package mcts;

import java.util.EnumMap;
import java.util.List;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public abstract class MCTree<M> {
    Selector selector;
    Selector best_move_selector = new UCBSelector(0, null);
    GuidedSimulator simulator;
    Backpropagator backpropagator;
    double ucb1_coef;
    MCNode root;

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
    public abstract boolean decisionNeeded();

    public void moveToNode(MCNode node) {
        assert node.parent==root;
        node.parent = null;
        root = node;
    }

    public void advanceTree(MOVE last_pacman_move, EnumMap<GHOST, MOVE> last_ghosts_moves) {
        root.ticks_to_go--;
        assert root.ticks_to_go>=-2;

        while (root.ticks_to_go==-1) {
            MCNode next_node;
            if (root.pacmanOnTurn()) {
                next_node = root.child(last_pacman_move);
            } else {
                next_node = root.child(last_ghosts_moves);
            }
            next_node.expand();
            root = next_node;
            root.ticks_to_go += -1; /* propagate -1 delay */
            root.parent = null; /* drop unreachable paths */
        }
    }

    public boolean isPacmanTree() {
        return root.isGhostsNode();
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
