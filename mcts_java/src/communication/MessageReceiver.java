package communication;

import communication.messages.Message;

public interface MessageReceiver {
    public boolean receiveQueueEmpty();
    public long receiveQueueSize();
    public int receiveQueueLength();
    public Message receive();
    public Channel channel();
}
