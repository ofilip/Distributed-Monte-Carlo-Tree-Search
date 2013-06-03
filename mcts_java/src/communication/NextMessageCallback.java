package communication;

import communication.messages.Message;

public interface NextMessageCallback {
    public Message next();
}
