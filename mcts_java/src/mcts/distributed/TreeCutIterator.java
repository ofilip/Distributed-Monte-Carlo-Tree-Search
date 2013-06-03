package mcts.distributed;

public class TreeCutIterator {
    TreeCutNode curr;

    TreeCutIterator(TreeCutNode curr) {
        this.curr = curr;
    }

    public TreeCutNode next() { TreeCutNode res = curr; curr = curr.next(); return res; }
    public TreeCutNode current() { return curr; }
}
