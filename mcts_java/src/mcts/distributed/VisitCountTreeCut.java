package mcts.distributed;

import java.util.LinkedList;
import mcts.Action;
import mcts.MCTree;
import utils.Triplet;

public class VisitCountTreeCut extends TreeCut {

    private VisitCountTreeCut(TreeCutNode nodes, long maxBytesSize, int visitCountThreshold, long size) {
        super(nodes, maxBytesSize, visitCountThreshold, size);
    }

    public static VisitCountTreeCut createRootCut(MCTree tree, long maxBytesSize, int visitCountThreshold) {
        return new VisitCountTreeCut(new TreeCutNode(tree.root(), new LinkedList<Action>()), maxBytesSize, visitCountThreshold, 1);
    }

    private TreeCutNode bestNode() {
        assert(nodes!=null);
        TreeCutNode best = nodes;
        TreeCutNode curr = nodes.next();
        while (curr!=nodes) {
            if (curr.treeNode().calculatedVisitCount()>best.treeNode().calculatedVisitCount()) {
                best = curr;
            }
            curr = curr.next();
        }

        return best;
    }

    @Override
    public void reexpand() {
        while (maxBytesSize()>bytesSize) {
            TreeCutNode maxNode = bestNode();
            if (maxNode.next()!=maxNode /* maxNode is not root (root is expanded immediately) */
                    && maxNode.treeNode().calculatedVisitCount()<visitCountThreshold()) break;
            Triplet<Long, Long, TreeCutNode> expanded = maxNode.expand();
            if (expanded==null) break; /* tree is yet too small */
            size += expanded.first;
            bytesSize += expanded.second;

            if (nodes==maxNode) {
                nodes = expanded.third;
            }

            /* actualize iterators */
            for (TreeCutIterator it: iterators) {
                if (it.curr==maxNode) {
                    it.curr = expanded.third;
                }
            }
        }
    }
}
