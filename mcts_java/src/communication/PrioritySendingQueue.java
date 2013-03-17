package communication;

import communication.messages.Message;
import java.util.EnumMap;
import java.util.LinkedList;

public class PrioritySendingQueue {
    private EnumMap<Priority, LinkedList<Message>> queues = new EnumMap<Priority, LinkedList<Message>>(Priority.class);
    private long buffer_size;
    public long size = 0;
    public long length = 0;
    
    {
        for (Priority p: Priority.values()) {
            queues.put(p, new LinkedList<Message>());
        }
    }
    
    public PrioritySendingQueue(long buffer_size) {
        this.buffer_size = buffer_size;
    }
    
    public long bufferSize() { return buffer_size; }
    public long size() { return size(); }
    public boolean isEmpty() { return size==0; }
    public long length() { return length(); }
    
    public Message removeFirst() {
        if (size==0) return null;
        for (LinkedList<Message> queue: queues.values()) {
            if (!queue.isEmpty()) {
                Message m = queue.removeFirst();
                size--;
                length -= m.length();
                return m;
            }
        }
        
        assert(false);
        return null;
    }
    
    private Message removeLast() {
        if (size==0) return null;
        for (Priority p: Priority.lowest2highest) {
            LinkedList<Message> queue = queues.get(p);
            if (!queue.isEmpty()) {
                Message m = queue.removeFirst();
                size--;
                length -= m.length();
                return m;
            }
        }
        
        assert(false);
        return null;
    }
    
    private void checkFullness() {
        if (length>buffer_size) {
            removeLast();
        }
    }
    
    public void add(Priority priority, Message message) {
        length += message.length();
        size++;
        queues.get(priority).add(message);
        checkFullness();
    }
    
    public void addFirst(Priority priority, Message message) {
        length += message.length();
        size++;
        queues.get(priority).addFirst(message);
        checkFullness();
    }    
    
    public void clear() {
        for (LinkedList<Message> queue: queues.values()) {
            queue.clear();
        }
    }
}
