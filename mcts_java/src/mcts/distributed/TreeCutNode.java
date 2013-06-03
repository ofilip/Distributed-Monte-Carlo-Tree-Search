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

public class TreeCutNode {
    private final MCNode node;
    private final ArrayList<Action> path;
    private TreeCutNode previous, next;

    protected TreeCutNode(final MCNode node, final ArrayList<Action> path) {
        this.node = node;
        this.path = path!=null? path: new ArrayList<Action>();
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

    public Pair<Long,TreeCutNode> expand() {
        long sizeDiff = -toMessage().length();
        if (!node.expanded()) return null;
        TreeCutNode curr = this;
        if (node.pacmanChildren()!=null) {
            assert(node.ghostsChildren()==null);
            for (MOVE move: node.pacmanChildren().keySet()) {
                Action action = new PacmanAction(move);
                ArrayList<Action> child_path = new ArrayList<Action>(path);
                child_path.add(action);
                curr = curr.append(new TreeCutNode(node.pacmanChildren().get(move), child_path));
                sizeDiff += curr.toMessage().length();
            }
        } else if (node.ghostsChildren()!=null) {
            for (EnumMap<GHOST,MOVE> move: node.ghostsChildren().keySet()) {
                Action action = new GhostAction(move);
                ArrayList<Action> child_path = new ArrayList<Action>(path);
                child_path.add(action);
                curr = curr.append(new TreeCutNode(node.ghostsChildren().get(move), child_path));
                sizeDiff += curr.toMessage().length();
            }
        } else assert(false);

        /* return first child */
        TreeCutNode firstChild = this.next;

        /* remove node from the list */
        this.next.previous = this.previous;
        this.previous.next = this.next;

        return new Pair<Long, TreeCutNode>(sizeDiff, firstChild);
    }

    public MCNode treeNode() { return node; }
    public TreeCutNode next() { return next; }
    public TreeCutNode previous() { return previous; }

    public TreeNodeMessage toMessage() {
        return new TreeNodeMessage(path, node.value(), node.visitCount());
    }
}
