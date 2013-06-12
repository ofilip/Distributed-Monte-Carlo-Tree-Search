package mcts.distributed;

public class TreeCutIterator {
    TreeCutNode curr;
    TreeCut parent;
    int iterationCount = 0;

    TreeCutIterator(TreeCutNode curr, TreeCut parent) {
        this.curr = curr;
        this.parent = parent;
    }

    public TreeCutNode next() {
        TreeCutNode res = curr; curr = curr.next();
        if (curr==parent.nodes) {
            iterationCount++;
        }
        return res;
    }
    public TreeCutNode current() { return curr; }
    public TreeCut parent() { return parent; }
    public double iterationCount() {
        TreeCutNode node = curr;
        int cnt = 0;
        while (node!=parent.nodes) {
            node = node.previous();
            cnt++;
        }
        return iterationCount + cnt/(double)parent.size;
    }
}
