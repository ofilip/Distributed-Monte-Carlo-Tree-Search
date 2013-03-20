
package communication;

import communication.messages.Message;

public class FullReliability implements Reliability {

    @Override
    public boolean isTransmitted(Message message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Reliability clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
