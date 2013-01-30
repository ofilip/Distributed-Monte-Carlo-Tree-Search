package communication;

import communication.messages.Message;

public interface MessageSender {
    public boolean sendQueueEmpty();
    public long sendQueueSize();
    public int sendQueueLength();
    public double secondsToSendAll();
    public void send(Message message);
    public Channel channel();
}
