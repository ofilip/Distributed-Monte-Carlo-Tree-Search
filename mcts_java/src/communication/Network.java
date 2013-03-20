package communication;

import java.util.HashMap;
import java.util.Map;
import utils.SystemTimer;
import utils.VirtualTimer;

public class Network {
    private long channel_transmission_speed;
    private VirtualTimer timer = SystemTimer.instance;
    private Map<String, Channel> channels = new HashMap<String, Channel>();
    private Reliability reliability;
    
    
    public Network(long channel_transmission_speed) {
        this.timer = timer;
    }
    
    public Channel openChannel(String name, long buffer_size) {
        if (!channels.containsKey(name)) {
            channels.put(name, new Channel(this, name, channel_transmission_speed, buffer_size, reliability.clone()));
        }
        return channels.get(name);
    }
    
    public MessageSender sender(String name) {
        return channels.get(name);
    }
    
    public MessageReceiver receiver(String name) {
        return channels.get(name);
    }
    
    public long channelTransmissionSpeed() {
        return channel_transmission_speed;
    }
    
    public VirtualTimer timer() {
        return timer;
    }
    
    public void setTimer(VirtualTimer timer) {
        if (timer==null) {
            this.timer = SystemTimer.instance;
        } else {
            this.timer = timer;
        }
    }
    
    public Reliability getReliability() { return reliability; }
    public void setReliability(Reliability reliability) {
        this.reliability = reliability;
    }
    
    public void reset() {
        for (Channel channel: channels.values()) {
            channel.clear();
        }
    }
}
