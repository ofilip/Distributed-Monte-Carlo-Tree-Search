package mcts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import mcts.Utils;
import pacman.game.Constants.*;
import pacman.game.Game;
import pacman.game.GameView;

public abstract class MCTree<M> {
    Selector selector;
    Selector best_move_selector = new UCBSelector(0, null);
    MySimulator simulator;
    Backpropagator backpropagator;
    double ucb1_coef;
    MCNode root;
    
    
    public MCNode root() {
        return root;
    }
    
    public void iterate() {
        MCNode node = root.select();
        if (node.isRoot()||!node.parent.game.wasPacManEaten()) {
            node.expand();
            double reward = node.simulate();
            node.backpropagate(reward);
        } else {
            /* do not extend subtree if pacman was eaten */
            node.parent.backpropagate(node.value);
        }
    }
    
    public MCTree(Game game, Selector selector, MySimulator simulator, Backpropagator backpropagator, double ucb1_coef) {
        this.selector = selector;
        this.simulator = simulator;
        this.backpropagator = backpropagator;
        this.ucb1_coef = ucb1_coef;
    }
    
    public abstract M bestMove(Game game);
    
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
                /////DEBUG////
                if (root.child(last_pacman_move)==null) {
                    int i = 1;
                }
                //////////////
                next_node = root.child(last_pacman_move);
            } else {                                
                /////DEBUG////
                if (root.child(last_ghosts_moves)==null) {
                    int i = 1;
                }
                //////////////
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
