package mcts.distributed;

import communication.messages.Message;
import communication.messages.TreeNodeMessage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import mcts.Action;
import mcts.GhostAction;
import mcts.MCNode;
import mcts.MCTree;
import mcts.PacmanAction;
import mcts.PacmanNode;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;
import utils.Triplet;

public class TreeCutNode {
    private final MCNode node;
    final LinkedList<Action> path;
    private TreeCutNode previous, next;

    protected TreeCutNode(final MCNode node, final LinkedList<Action> path) {
        this.node = node;
        this.path = path!=null? path: new LinkedList<Action>();
        this.next = this.previous = this;
    }

    public static TreeCutNode createRootCut(MCTree tree) {
        return new TreeCutNode(tree.root(), null);
    }

    private TreeCutNode append(TreeCutNode node) {
        node.next = this.next;
        node.next.previous = node;
        node.previous = this;
        node.previous.next = node;
        return node;
    }

    public Triplet<Long,Long,TreeCutNode> expand() {
        long byteSizeDiff = -toMessage().length();
        long sizeDiff = -1;
        if (!node.expanded()) return null;
        TreeCutNode curr = this;
        if (node.pacmanChildren()!=null) {
            assert(node.ghostsChildren()==null);
            for (MOVE move: node.pacmanChildren().keySet()) {
                Action action = new PacmanAction(move);
                LinkedList<Action> child_path = new LinkedList<Action>(path);
                child_path.add(action);
                curr = curr.append(new TreeCutNode(node.pacmanChildren().get(move), child_path));
                byteSizeDiff += curr.toMessage().length();
                sizeDiff++;
            }
        } else if (node.ghostsChildren()!=null) {
            for (EnumMap<GHOST,MOVE> move: node.ghostsChildren().keySet()) {
                Action action = new GhostAction(move);
                LinkedList<Action> child_path = new LinkedList<Action>(path);
                child_path.add(action);
                curr = curr.append(new TreeCutNode(node.ghostsChildren().get(move), child_path));
                byteSizeDiff += curr.toMessage().length();
                sizeDiff++;
            }
        } else assert(false);

        /* return first child */
        TreeCutNode firstChild = this.next;

        /* remove node from the list */
        this.next.previous = this.previous;
        this.previous.next = this.next;

        return new Triplet<Long, Long, TreeCutNode>(sizeDiff, byteSizeDiff, firstChild);
    }

    public MCNode treeNode() { return node; }
    public TreeCutNode next() { return next; }
    public TreeCutNode previous() { return previous; }

    public TreeNodeMessage toMessage() {
        //Pair<Double,Integer> nodeValue = node.calculated();
        //return new TreeNodeMessage(path, nodeValue.first, nodeValue.second);
        return new TreeNodeMessage(path, node.calculatedValue(), node.calculatedVisitCount());
    }
}
