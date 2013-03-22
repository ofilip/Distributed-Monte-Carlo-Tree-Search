
package communication;

import communication.messages.Message;

public class FullReliability implements Reliability {

    @Override
    public boolean isTransmitted(Message message) {
        return true;
    }

    @Override
    public Reliability clone() {
        return new FullReliability();
    }

}
