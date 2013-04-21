package communication;

import java.util.HashMap;
import java.util.Map;
import mcts.Constants;
import utils.SystemTimer;
import utils.VirtualTimer;

public class Network {
    private long channelTransmissionSpeed = Constants.DEFAULT_CHANNEL_TRANSMISSION_SPEED;
    private VirtualTimer timer = SystemTimer.instance;
    private Map<String, Channel> channels = new HashMap<String, Channel>();
    private Reliability reliability = new FullReliability();


    public Network() {
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public Network(long channelTransmissionSpeed) {
        this.channelTransmissionSpeed = channelTransmissionSpeed;
    }

    public Channel openChannel(String name, long buffer_size) {
        if (!channels.containsKey(name)) {
            channels.put(name, new Channel(this, name, channelTransmissionSpeed, buffer_size, reliability.clone()));
        }
        return channels.get(name);
    }

    public MessageSender sender(String name) {
        return channels.get(name);
    }

    public MessageReceiver receiver(String name) {
        return channels.get(name);
    }

    public long getChannelTransmissionSpeed() {
        return channelTransmissionSpeed;
    }

    public void setChannelTransmissionSpeed(long channelTransmissionSpeed) {
        this.channelTransmissionSpeed = channelTransmissionSpeed;
    }

    public VirtualTimer timer() {
        return timer;
    }

    public synchronized void setTimer(VirtualTimer timer) {
        if (timer==null) {
            this.timer = SystemTimer.instance;
        } else {
            this.timer = timer;
        }
    }

    public Reliability getReliability() { return reliability; }
    public synchronized void setReliability(Reliability reliability) {
        this.reliability = reliability;
    }

    public synchronized void clearUnsent() {
        for (Channel channel: channels.values()) {
            channel.flush();
        }
    }

    public synchronized void clear() {
        for (Channel channel: channels.values()) {
            channel.flush();
        }
    }
}
