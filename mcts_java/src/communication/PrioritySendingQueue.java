package communication;

import communication.messages.Message;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;

public class PrioritySendingQueue {
    private EnumMap<Priority, LinkedList<Message>> queues = new EnumMap<Priority, LinkedList<Message>>(Priority.class);
    private long bufferSize;
    public long count = 0;
    public long length = 0;

    {
        for (Priority p: Priority.values()) {
            queues.put(p, new LinkedList<Message>());
        }
    }

    public PrioritySendingQueue(long buffer_size) {
        this.bufferSize = buffer_size;
    }

    public long bufferSize() { return bufferSize; }
    public long itemsCount() { return count; }
    public boolean isEmpty() { return count==0; }
    public long length() { return length; }

    public Message removeFirst() {
        if (count==0) return null;
        for (LinkedList<Message> queue: queues.values()) {
            if (!queue.isEmpty()) {
                Message m = queue.removeFirst();
                count--;
                length -= m.length();
                return m;
            }
        }

        assert(false);
        return null;
    }

    private Message removeLast() {
        if (count==0) return null;
        for (Priority p: Priority.lowest2highest) {
            LinkedList<Message> queue = queues.get(p);
            if (!queue.isEmpty()) {
                Message m = queue.removeLast();
                count--;
                length -= m.length();
                return m;
            }
        }

        assert(false);
        return null;
    }

    private void checkFullness() {
        while (length>bufferSize) {
            Message dropped = removeLast();
            dropped.onMessageDropped();
        }
    }

    public void add(Priority priority, Message message) {
        length += message.length();
        count++;
        queues.get(priority).add(message);
        checkFullness();
    }

    public void addFirst(Priority priority, Message message) {
        length += message.length();
        count++;
        queues.get(priority).addFirst(message);
        checkFullness();
    }

    public void flush() {
        for (LinkedList<Message> queue: queues.values()) {
            for (Message message: queue) {
                message.onMessageDropped();
            }
            queue.clear();
            length = 0;
            count = 0;
        }
    }

    public void flush(Class messageClass) {
        for (LinkedList<Message> queue: queues.values()) {
            for (Iterator<Message> it = queue.iterator(); it.hasNext(); ) {
                Message message = it.next();
                if (messageClass.isInstance(message)) {
                    count--;
                    length -= message.length();
                    it.remove();
                    message.onMessageDropped();
                }
            }
        }
    }
}
