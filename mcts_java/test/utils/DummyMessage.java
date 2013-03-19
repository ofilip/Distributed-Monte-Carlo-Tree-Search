/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import communication.messages.Message;

/**
 *
 * @author ondra
 */
public class DummyMessage extends Message {
    private long length;
    
    private DummyMessage() { super("dummy"); }
    public DummyMessage(long length) { super("dummy"); this.length = length; }
    
    @Override public long length() { return length; }
    
}
