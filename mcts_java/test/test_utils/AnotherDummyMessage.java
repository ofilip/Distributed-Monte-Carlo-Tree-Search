package test_utils;

import communication.messages.Message;

public class AnotherDummyMessage extends Message {
    private long length;

    private AnotherDummyMessage() { super("dummy"); }
    public AnotherDummyMessage(long length) { super("dummy"); this.length = length; }

    @Override public long length() { return length; }

}
