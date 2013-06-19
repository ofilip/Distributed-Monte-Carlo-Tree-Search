package mcts.distributed;

import java.util.LinkedList;
import mcts.Action;
import mcts.MCTree;
import utils.Triplet;

public final class VisitCountTreeCut extends TreeCut {
    boolean aggregated;

    private VisitCountTreeCut(TreeCutNode nodes, long maxBytesSize, int visitCountThreshold, long size, boolean aggregated) {
        super(nodes, maxBytesSize, visitCountThreshold, size);
        reexpand();
        this.aggregated = aggregated;
    }

    public static VisitCountTreeCut createRootCut(MCTree tree, long maxBytesSize, int visitCountThreshold, boolean aggregated) {
        return new VisitCountTreeCut(new TreeCutNode(tree.root(), new LinkedList<Action>()), maxBytesSize, visitCountThreshold, 1, aggregated);
    }

    public boolean isAggregated() { return aggregated; } /* indicated whether maxBytesSize refers to total size of nodes or aggregated size */

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

    private long sizeToCompare() {
        return aggregated? aggregatedByteSize(): bytesSize;
    }

    @Override
    public void reexpand() {
        while (maxBytesSize()>sizeToCompare()) {
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
