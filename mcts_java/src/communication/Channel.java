package communication;

import communication.messages.Message;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import utils.SystemTimer;
import utils.VirtualTimer;

//TODO:
// * costs of broadcast
public class Channel implements MessageSender, MessageReceiver {
    private Network network;
    private long transmissionSpeed; /* bytes per second */
    private PrioritySendingQueue sendingQueue;
    private LinkedList<Message> receivedQueue = new LinkedList<Message>();
    private Message transmittedMessage = null; /* message being currently transmited, null if no transmission in progress */
    private long queueMillibytesTransmitted; /* millibytes to be transmitted from sending_queue to received_queue */
    private String name;
    private long lastTransmissionTime; /* time when last transmission happened */
    private Reliability reliability = new FullReliability();
    private long transmittedTotal = 0;
    private long transmittedSuccessfully = 0;

    private final static FullReliability FULL_RELIABIILTY = new FullReliability();

    protected Channel(Network network, String name, long transmissionSpeed, long bufferSize, Reliability reliability) {
        this.network = network;
        this.lastTransmissionTime = network.timer().currentVirtualMillis();
        this.name = name;
        this.transmissionSpeed = transmissionSpeed;
        this.sendingQueue = new PrioritySendingQueue(bufferSize);
        this.reliability = reliability==null? FULL_RELIABIILTY: reliability;
    }

    private void doTransmission() {
        long currentTime = network.timer().currentVirtualMillis();
        queueMillibytesTransmitted += (currentTime-lastTransmissionTime)*getTransmissionSpeed();

        if (transmittedMessage==null) {
            transmittedMessage = sendingQueue.removeFirst();
            if (transmittedMessage!=null) {
                transmittedMessage.onSendingStarted();
            }
        }

        while (transmittedMessage!=null&&1000*transmittedMessage.length()<queueMillibytesTransmitted) {
            queueMillibytesTransmitted -= 1000*transmittedMessage.length();
            transmittedTotal += transmittedMessage.length();
            if (reliability.isTransmitted(transmittedMessage)) { //TODO cover with tests
                receivedQueue.add(transmittedMessage);
                transmittedSuccessfully += transmittedMessage.length();
            }

            transmittedMessage = sendingQueue.removeFirst();
            if (transmittedMessage!=null) {
                transmittedMessage.onSendingStarted();
            }
        }

        if (transmittedMessage==null) {
            queueMillibytesTransmitted = 0;
        }

        lastTransmissionTime = currentTime;
    }

    @Override
    synchronized public boolean sendQueueEmpty() {
        doTransmission();
        return transmittedMessage==null;
    }

    @Override
    synchronized public boolean receiveQueueEmpty() {
        doTransmission();
        return receivedQueue.isEmpty();
    }

    @Override
    synchronized public long receiveQueueItemsCount() {
        doTransmission();
        return receivedQueue.size();
    }

    @Override
    synchronized public long receiveQueueLength() {
        doTransmission();

        long size = 0;
        for (Message message: receivedQueue) {
            size += message.length();
        }
        return size;

    }

    @Override
    synchronized public Message receive() {
        doTransmission();
        return receivedQueue.pollFirst();
    }

    synchronized private long sendQueueMillisLength() {
        doTransmission();
        long size = 0;
        if (transmittedMessage!=null) size += transmittedMessage.length();
        size += sendingQueue.length();
        return 1000*size-queueMillibytesTransmitted;
    }

    @Override
    synchronized public long sendQueueItemsCount() {
        doTransmission();
        return transmittedMessage==null? 0: 1+sendingQueue.itemsCount();
    }

    @Override
    synchronized public long sendQueueLength() {
        return (long)Math.ceil(sendQueueMillisLength()/1000.0);
    }

    @Override
    synchronized public double secondsToSendAll() {
        return 0.001*sendQueueMillisLength()/getTransmissionSpeed();
    }

    @Override
    synchronized public void send(Priority priority, Message message) {
        if (sendingQueue.isEmpty()) lastTransmissionTime = network.timer().currentVirtualMillis(); /* flush transmission if queue is empty */
        sendingQueue.add(priority, message);
        doTransmission();
    }

    @Override
    synchronized public void sendFirst(Priority priority, Message message) {
        if (sendingQueue.isEmpty()) lastTransmissionTime = network.timer().currentVirtualMillis(); /* flush transmission if queue is empty */
        sendingQueue.addFirst(priority, message);
        doTransmission();
    }

    public long transmissionSpeed() {
        return getTransmissionSpeed();
    }

    public MessageSender sender() {
        return this;
    }

    public MessageReceiver receiver() {
        return this;
    }

    @Override
    public Channel channel() {
        return this;
    }

    public String name() {
        return name;
    }

    public synchronized void receiveQueueFlush() {
        receivedQueue.clear();
    }

    @Override
    public synchronized void sendQueueFlush() {
        sendingQueue.flush();
        transmittedMessage = null;
        queueMillibytesTransmitted = 0;
    }

    @Override
    public synchronized void sendQueueFlushUnsent() {
        sendingQueue.flush();
    }

    @Override
    public synchronized void sendQueueFlushUnsent(Class messageClass) {
        sendingQueue.flush(messageClass);
    }

    public synchronized void flushUnsent() {
        receiveQueueFlush();
        sendQueueFlushUnsent();
    }

    public synchronized void flush() {
        receiveQueueFlush();
        sendQueueFlush();
    }

    public long transmittedTotal() {
        return transmittedTotal;
    }

    public long transmittedSuccessfully() {
        return transmittedSuccessfully;
    }

    /**
     * @return the transmissionSpeed
     */
    public long getTransmissionSpeed() {
        return transmissionSpeed;
    }

    /**
     * @param transmissionSpeed the transmissionSpeed to set
     */
    public void setTransmissionSpeed(long transmissionSpeed) {
        this.transmissionSpeed = transmissionSpeed;
    }

    
}
