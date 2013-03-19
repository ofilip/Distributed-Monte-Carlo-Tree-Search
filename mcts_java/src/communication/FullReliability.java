package communication;

import communication.messages.Message;

public class FullReliability implements Reliability {    
    @Override
    public boolean isTransmitted(Message message) {
        return true;
    }
    
    @Override
    public FullReliability clone() {
        return new FullReliability();
    }
}
