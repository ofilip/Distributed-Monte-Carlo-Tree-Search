package mcts.distributed;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public abstract class TreeCut {
    TreeCutNode nodes;
    protected long bytesSize; /* bytes necessary to transmit whole cut */
    private long maxBytesSize;
    private int visitCountThreshold; /* Minimal visit count of node to be expanded in cut */
    protected Set<TreeCutIterator> iterators = new HashSet<TreeCutIterator>();
    protected long size;

    public long size() { return size; }
    public long bytesSize() {return bytesSize;}
    public void setMaxBytesSize(long bytes) {this.maxBytesSize = bytes;}
    public long maxBytesSize() {return maxBytesSize;}
    public boolean capacityFull() { return bytesSize>=maxBytesSize; }
    public int visitCountThreshold() {return visitCountThreshold;}
    public TreeCutNode nodes() { return nodes; }

    public TreeCutIterator registerIterator() {
        TreeCutIterator it = new TreeCutIterator(nodes, this);
        iterators.add(it);
        return it;

    }

//    public void advance(int steps) {
//        TreeCutNode node = nodes;
//
//        do {
//            for (int i=0; i<steps; i++) {
//                node.path.removeFirst();
//            }
//            node = node.next();
//        } while (node!=nodes);
//    }

    protected TreeCut(TreeCutNode nodes, long maxBytesSize, int visitCountThreshold, long size) {
        this.nodes = nodes;
        this.maxBytesSize = maxBytesSize;
        this.visitCountThreshold = visitCountThreshold;
        bytesSize = nodes.toMessage().length();
        for (TreeCutNode n = nodes.next(); n!=nodes; n = n.next()) {
            bytesSize += n.toMessage().length();
        }
        this.size = size;
    }

    public abstract void reexpand();
}
