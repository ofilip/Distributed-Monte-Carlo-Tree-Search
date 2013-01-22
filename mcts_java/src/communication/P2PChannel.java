package communication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class P2PChannel<M extends Message> implements MessageSender<M>, MessageReceiver<M> {
    private long transmission_speed; /* bytes per second */
    private LinkedList<M> sending_queue = new LinkedList<M>();
    private LinkedList<M> received_queue = new LinkedList<M>();
    private long last_transmission_time = System.currentTimeMillis(); /* time when last transmission happened */
    private long queue_millibytes_transmitted; /* millibytes to be transmitted from sending_queue to received_queue */
    private String name;
    
    public P2PChannel(String name, long transmission_speed) {
        this.name = name;
        this.transmission_speed = transmission_speed;
    }
    
    private void doTransmission() {
        long current_time = System.currentTimeMillis();
        queue_millibytes_transmitted += (last_transmission_time-current_time)*transmission_speed;
        
        while (!sending_queue.isEmpty()) {
            M top = sending_queue.getFirst();
            if (1000*top.length()<queue_millibytes_transmitted) {
                queue_millibytes_transmitted -= 1000*top.length();
                sending_queue.removeFirst();
                received_queue.add(top);
            } else {
                break;
            }
        }
        
        if (sending_queue.isEmpty()) {
            queue_millibytes_transmitted = 0;
        }
        
        last_transmission_time = current_time;
    }
    
    @Override
    public boolean sendQueueEmpty() {
        doTransmission();
        return sending_queue.isEmpty();
    }
    
    @Override
    public boolean receiveQueueEmpty() {
        doTransmission();
        return received_queue.isEmpty();
    }
    
    @Override
    public long receiveQueueSize() {
        doTransmission();
        long size = queue_millibytes_transmitted/1000;
        for (M message: received_queue) {
            size += message.length();
        }
        assert size>=0;
        return size;
    }
    
    @Override
    public int receiveQueueLength() {
        doTransmission();
        return received_queue.size();
    }            
    
    @Override
    public M receive() {
        doTransmission();
        return received_queue.pollFirst();
    }
    
    private long sendQueueMillisSize() {
        doTransmission();
        long size = -queue_millibytes_transmitted;
        for (M message: sending_queue) {
            size += 1000*message.length();
        }
        assert size>=0;
        return size;
    }
    
    @Override
    public long sendQueueSize() {
        return sendQueueMillisSize()/1000;
    }
    
    @Override
    public int sendQueueLength() {
        doTransmission();
        return sending_queue.size();
    }
    
    @Override
    public double secondsToSendAll() {
        return 1000*sendQueueMillisSize()/transmission_speed;
    }
    
    @Override
    public void send(M message) {
        sending_queue.add(message);
    }    
    
    public long transmissionSpeed() {
        return transmission_speed;
    }
    
    public MessageSender<M> sender() {
        return this;
    }
    
    public MessageReceiver<M> receiver() {
        return this;
    }
    
    @Override
    public P2PChannel channel() {
        return this;
    }
    
    public String name() {
        return name;
    }
}
