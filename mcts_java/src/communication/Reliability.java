package communication;

import communication.messages.Message;

public interface Reliability extends Cloneable {
    public boolean isTransmitted(Message message);
    
    public Reliability clone();
}
