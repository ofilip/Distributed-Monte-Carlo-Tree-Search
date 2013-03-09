package communication;

import communication.messages.Message;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import utils.SystemTimer;
import utils.VirtualTimer;

public class Channel implements MessageSender, MessageReceiver {
    private Network network;
    private long transmission_speed; /* bytes per second */
    private LinkedList<Message> sending_queue = new LinkedList<Message>();
    private LinkedList<Message> received_queue = new LinkedList<Message>();
    private long queue_millibytes_transmitted; /* millibytes to be transmitted from sending_queue to received_queue */
    private String name;
    private long last_transmission_time; /* time when last transmission happened */
    
    protected Channel(Network network, String name, long transmission_speed) {
        this.network = network;
        this.last_transmission_time = network.timer().currentMillis();
        this.name = name;
        this.transmission_speed = transmission_speed;
    }
    
    private void doTransmission() {
        long current_time = network.timer().currentMillis();
        queue_millibytes_transmitted += (current_time-last_transmission_time)*transmission_speed;
        
        while (!sending_queue.isEmpty()) {
            Message top = sending_queue.getFirst();
            
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
    synchronized public boolean sendQueueEmpty() {
        doTransmission();
        return sending_queue.isEmpty();
    }
    
    @Override
    synchronized public boolean receiveQueueEmpty() {
        doTransmission();
        return received_queue.isEmpty();
    }
    
    @Override
    synchronized public long receiveQueueSize() {
        doTransmission();
        long size = queue_millibytes_transmitted/1000;
        for (Message message: received_queue) {
            size += message.length();
        }
        assert size>=0;
        return size;
    }
    
    @Override
    synchronized public int receiveQueueLength() {
        doTransmission();
        return received_queue.size();
    }            
    
    @Override
    synchronized public Message receive() {
        doTransmission();
        return received_queue.pollFirst();
    }
    
    synchronized private long sendQueueMillisSize() {
        doTransmission();
        long size = -queue_millibytes_transmitted;
        for (Message message: sending_queue) {
            size += 1000*message.length();
        }
        assert size>=0;
        return size;
    }
    
    @Override
    synchronized public long sendQueueSize() {
        return sendQueueMillisSize()/1000;
    }
    
    @Override
    synchronized public int sendQueueLength() {
        doTransmission();
        return sending_queue.size();
    }
    
    @Override
    synchronized public double secondsToSendAll() {
        return 1000*sendQueueMillisSize()/transmission_speed;
    }
    
    @Override
    synchronized public void send(Message message) {
        if (sending_queue.isEmpty()) last_transmission_time = network.timer().currentMillis(); /* reset transmission if queue is empty */
        sending_queue.add(message);
    }    
    
    public long transmissionSpeed() {
        return transmission_speed;
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
}
