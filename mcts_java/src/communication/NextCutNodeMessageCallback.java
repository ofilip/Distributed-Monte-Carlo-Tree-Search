package communication;

import communication.messages.Message;
import mcts.distributed.TreeCut;
import mcts.distributed.TreeCutIterator;

public class NextCutNodeMessageCallback implements NextMessageCallback {
    TreeCutIterator messageIterator;
    public NextCutNodeMessageCallback(TreeCut cut) {
       messageIterator = cut.registerIterator();
    }

    public Message next() {
        return messageIterator.next().toMessage();
    }
}
