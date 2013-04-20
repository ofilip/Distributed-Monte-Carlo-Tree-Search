package communication;

import communication.messages.Message;

public interface MessageSender {
    public boolean sendQueueEmpty();
    public long sendQueueItemsCount();
    public long sendQueueLength();
    public void sendQueueFlush();
    public void sendQueueFlushUnsent();
    public void sendQueueFlushUnsent(Class messageClass);
    public double secondsToSendAll();
    public void send(Priority priority, Message message);
    public void sendFirst(Priority priority, Message message);
    public Channel channel();
}
