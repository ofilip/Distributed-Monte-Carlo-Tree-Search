package mcts.distributed;

import java.util.ArrayList;
import java.util.TreeMap;
import mcts.Action;
import mcts.MCNode;
import mcts.MCTree;
import utils.Pair;

public class VisitCountTreeCut extends TreeCut {

    private VisitCountTreeCut(TreeCutNode nodes, long maxBytesSize, int visitCountThreshold) {
        super(nodes, maxBytesSize, visitCountThreshold);
    }

    public static VisitCountTreeCut createRootCut(MCTree tree, long maxBytesSize, int visitCountThreshold) {
        return new VisitCountTreeCut(new TreeCutNode(tree.root(), new ArrayList<Action>()), maxBytesSize, visitCountThreshold);
    }

    private TreeCutNode bestNode() {
        assert(nodes!=null);
        TreeCutNode best = nodes;
        TreeCutNode curr = nodes.next();
        while (curr!=nodes) {
            if (curr.treeNode().visitCount()>best.treeNode().visitCount()) {
                best = curr;
            }
        }

        return best;
    }

    @Override
    public void reexpand() {
        while (maxBytesSize()>bytesSize) {
            TreeCutNode maxNode = bestNode();
            if (maxNode.treeNode().visitCount()<visitCountThreshold()) break;
            Pair<Long, TreeCutNode> expanded = maxNode.expand();
            if (expanded==null) break; /* tree is yet too small */
            bytesSize += expanded.first;

            /* actualize iterators */
            for (TreeCutIterator it: iterators) {
                if (it.curr==maxNode) {
                    it.curr = expanded.second;
                }
            }
        }
    }
}
