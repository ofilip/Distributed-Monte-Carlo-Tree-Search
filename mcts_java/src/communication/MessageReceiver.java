package communication;

public interface MessageReceiver<M extends Message> {
    public boolean receiveQueueEmpty();
    public long receiveQueueSize();
    public int receiveQueueLength();
    public M receive();
    public P2PChannel channel();
}
