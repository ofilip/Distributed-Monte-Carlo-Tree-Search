package communication;

import java.util.HashMap;
import java.util.Map;

public class P2PNetwork<M extends Message> {
    private long channel_transmission_speed;
    Map<String, P2PChannel<M>> channels = new HashMap<String, P2PChannel<M>>();
    
    public P2PNetwork(long channel_transmission_speed) {
        this.channel_transmission_speed = channel_transmission_speed;
    }
    
    public P2PChannel<M> openChannel(String name) {
        if (!channels.containsKey(name)) {
            channels.put(name, new P2PChannel<M>(name, channel_transmission_speed));
        }
        return channels.get(name);
    }
    
    public MessageSender<M> sender(String name) {
        return openChannel(name);
    }
    
    public MessageReceiver<M> receiver(String name) {
        return openChannel(name);
    }
    
    public long channelTransmissionSpeed() {
        return channel_transmission_speed;
    }
}
