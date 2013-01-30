package communication;

import java.util.HashMap;
import java.util.Map;

public class Network {
    private long channel_transmission_speed;
    Map<String, Channel> channels = new HashMap<String, Channel>();
    
    public Network(long channel_transmission_speed) {
        this.channel_transmission_speed = channel_transmission_speed;
    }
    
    public Channel openChannel(String name) {
        if (!channels.containsKey(name)) {
            channels.put(name, new Channel(name, channel_transmission_speed));
        }
        return channels.get(name);
    }
    
    public MessageSender sender(String name) {
        return openChannel(name);
    }
    
    public MessageReceiver receiver(String name) {
        return openChannel(name);
    }
    
    public long channelTransmissionSpeed() {
        return channel_transmission_speed;
    }
}
