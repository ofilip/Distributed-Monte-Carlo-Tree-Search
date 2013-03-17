package communication;

import communication.messages.Message;

public interface MessageReceiver {
    public boolean receiveQueueEmpty();
    public long receiveQueueSize();
    public long receiveQueueLength();
    public Message receive();
    public Channel channel();
}
