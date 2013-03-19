package communication;

import java.util.HashMap;
import java.util.Map;
import utils.SystemTimer;
import utils.VirtualTimer;

public class Network {
    private long channel_transmission_speed;
    private VirtualTimer timer;
    private Map<String, Channel> channels = new HashMap<String, Channel>();
    
    public Network(long channel_transmission_speed) {
        this(channel_transmission_speed, SystemTimer.instance);
    }
    
    public Network(long channel_transmission_speed, VirtualTimer timer) {
        this.timer = timer;
        this.channel_transmission_speed = channel_transmission_speed;
    }
    
    public Channel openChannel(String name, long buffer_size) {
        if (!channels.containsKey(name)) {
            channels.put(name, new Channel(this, name, channel_transmission_speed, buffer_size));
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
    
    public void reset() {
        for (Channel channel: channels.values()) {
            channel.clear();
        }
    }
}
