package communication;

import communication.messages.Message;

public interface Reliability {
    public boolean isTransmitted(Message message);
}
