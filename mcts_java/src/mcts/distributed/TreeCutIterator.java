package mcts.distributed;

public class TreeCutIterator {
    TreeCutNode curr;
    TreeCut parent;

    TreeCutIterator(TreeCutNode curr, TreeCut parent) {
        this.curr = curr;
        this.parent = parent;
    }

    public TreeCutNode next() { TreeCutNode res = curr; curr = curr.next(); return res; }
    public TreeCutNode current() { return curr; }
    public TreeCut parent() { return parent; }
}
