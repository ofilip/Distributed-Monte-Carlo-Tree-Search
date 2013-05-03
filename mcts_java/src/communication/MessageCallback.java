package communication;

import communication.messages.Message;
import mcts.distributed.agents.GhostAgent;

public interface MessageCallback {
    public void call(Message message);
}
