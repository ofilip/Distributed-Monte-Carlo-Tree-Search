package communication;

import communication.messages.Message;

public interface MessageSender {
    public boolean sendQueueEmpty();
    public long sendQueueSize();
    public long sendQueueLength();
    public double secondsToSendAll();
    public void send(Priority priority, Message message);
    public void sendFirst(Priority priority, Message message);
    public Channel channel();
}
