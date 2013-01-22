package communication;

public interface MessageSender<M extends Message> {
    public boolean sendQueueEmpty();
    public long sendQueueSize();
    public int sendQueueLength();
    public double secondsToSendAll();
    public void send(M message);
    public P2PChannel channel();
}
