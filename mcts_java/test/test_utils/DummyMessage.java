package test_utils;

import communication.messages.Message;

public class DummyMessage extends Message {
    private long length;

    private DummyMessage() { super("dummy"); }
    public DummyMessage(long length) { super("dummy"); this.length = length; }

    @Override public long length() { return length; }

}
