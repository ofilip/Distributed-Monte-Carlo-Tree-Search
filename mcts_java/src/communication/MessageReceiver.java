package communication;

import communication.messages.Message;

public interface MessageReceiver {
    public boolean receiveQueueEmpty();
    public long receiveQueueItemsCount();
    public long receiveQueueLength();
    public void receiveQueueFlush();
    public Message receive();
    public Channel channel();
}
