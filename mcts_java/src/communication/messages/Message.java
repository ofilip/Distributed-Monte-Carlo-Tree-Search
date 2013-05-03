package communication.messages;

import communication.DummyMessageCallback;
import communication.MessageCallback;

public abstract class Message {
    protected String name;
    private MessageCallback onSendingStarted = new DummyMessageCallback();
    private MessageCallback onMessageDropped = new DummyMessageCallback();

    protected Message(String name) {
        this.name = name;
    }

    /**
     *
     * @return Message length in bytes.
     */
    public abstract long length();

    /**
     *
     * @return Message getName.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the onSuccessfullySent
     */
    public void onSendingStarted() {
        onSendingStarted.call(this);
    }

    /**
     * @param onSuccessfullySent the onSuccessfullySent to set
     */
    public void onSendingStarted(MessageCallback onSuccessfullySent) {
        this.onSendingStarted = onSuccessfullySent;
    }

    public void onMessageDropped() {
        onMessageDropped.call(this);
    }

    public void onMessageDropped(MessageCallback onMessageDropped) {
        this.onMessageDropped = onMessageDropped;
    }

}
