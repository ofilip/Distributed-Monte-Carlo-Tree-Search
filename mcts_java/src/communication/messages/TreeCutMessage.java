package communication.messages;

import java.util.ArrayList;
import java.util.List;
import mcts.distributed.TreeCut;
import mcts.distributed.TreeCutNode;

public class TreeCutMessage extends Message {
    long length;
    List<TreeNodeMessage> messages = new ArrayList<TreeNodeMessage>();

    public TreeCutMessage(TreeCut cut) {
        super("tree_cut");
        this.length = cut.aggregatedByteSize();
        TreeCutNode node = cut.nodes();
        do {
            messages.add(node.toMessage());
            node = node.next();
        } while(node!=cut.nodes());
    }

    public List<TreeNodeMessage> nodeMessages() { return messages; }

    @Override
    public long length() {
        return length;
    }
}
