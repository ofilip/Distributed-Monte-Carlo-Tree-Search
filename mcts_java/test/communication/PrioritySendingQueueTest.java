package communication;

import communication.messages.Message;
import test_utils.DummyMessage;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrioritySendingQueueTest {

    @Test
    public void testBufferSize() {
        PrioritySendingQueue queue = new PrioritySendingQueue(1000);

        assertEquals(1000, queue.bufferSize());

        queue.add(Priority.MEDIUM, new DummyMessage(1001));

        assertEquals(0, queue.itemsCount());

        queue.add(Priority.MEDIUM, new DummyMessage(200));
        queue.add(Priority.MEDIUM, new DummyMessage(200));
        queue.add(Priority.MEDIUM, new DummyMessage(200));
        queue.add(Priority.MEDIUM, new DummyMessage(200));
        queue.add(Priority.MEDIUM, new DummyMessage(200));
        assertEquals(5, queue.itemsCount());

        queue.add(Priority.MEDIUM, new DummyMessage(200));
        assertEquals(5, queue.itemsCount());
        assertEquals(1000, queue.length());

        queue.add(Priority.MEDIUM, new DummyMessage(200));
        assertEquals(5, queue.itemsCount());
        assertEquals(1000, queue.length());

        queue.add(Priority.MEDIUM, new DummyMessage(200));
        assertEquals(5, queue.itemsCount());
        assertEquals(1000, queue.length());

        queue.addFirst(Priority.MEDIUM, new DummyMessage(100));
        assertEquals(5, queue.itemsCount());
        assertEquals(900, queue.length());

        queue.addFirst(Priority.MEDIUM, new DummyMessage(100));
        assertEquals(6, queue.itemsCount());
        assertEquals(1000, queue.length());

        queue.addFirst(Priority.MEDIUM, new DummyMessage(100));
        assertEquals(6, queue.itemsCount());
        assertEquals(900, queue.length());

        queue.addFirst(Priority.MEDIUM, new DummyMessage(100));
        assertEquals(7, queue.itemsCount());
        assertEquals(1000, queue.length());

        queue.addFirst(Priority.LOW, new DummyMessage(50));
        assertEquals(7, queue.itemsCount());
        assertEquals(1000, queue.length());

        queue.add(Priority.HIGH, new DummyMessage(50));
        assertEquals(7, queue.itemsCount());
        assertEquals(850, queue.length());

        queue.add(Priority.MEDIUM, new DummyMessage(50));
        assertEquals(8, queue.itemsCount());
        assertEquals(900, queue.length());
    }

    @Test
    public void testItems() {
        PrioritySendingQueue queue = new PrioritySendingQueue(1000);

        assertEquals(true, queue.isEmpty());
        assertEquals(0, queue.itemsCount());
        assertEquals(0, queue.length());

        queue.add(Priority.MEDIUM, new DummyMessage(4));

        assertEquals(false, queue.isEmpty());
        assertEquals(1, queue.itemsCount());
        assertEquals(4, queue.length());

        queue.add(Priority.LOW, new DummyMessage(2));
        queue.add(Priority.HIGH, new DummyMessage(16));
        queue.addFirst(Priority.MEDIUM, new DummyMessage(8));
        queue.addFirst(Priority.LOWEST, new DummyMessage(1));

        assertEquals(false, queue.isEmpty());
        assertEquals(5, queue.itemsCount());
        assertEquals(31, queue.length());

        queue.removeFirst();

        assertEquals(false, queue.isEmpty());
        assertEquals(4, queue.itemsCount());
        assertEquals(15, queue.length());

        queue.removeFirst();

        assertEquals(false, queue.isEmpty());
        assertEquals(3, queue.itemsCount());
        assertEquals(7, queue.length());

        queue.removeFirst();

        assertEquals(false, queue.isEmpty());
        assertEquals(2, queue.itemsCount());
        assertEquals(3, queue.length());

        queue.removeFirst();

        assertEquals(false, queue.isEmpty());
        assertEquals(1, queue.itemsCount());
        assertEquals(1, queue.length());

        queue.removeFirst();

        assertEquals(true, queue.isEmpty());
        assertEquals(0, queue.itemsCount());
        assertEquals(0, queue.length());
    }

    @Test
    public void testPriorities() {
        PrioritySendingQueue queue = new PrioritySendingQueue(1000);

        Message messages[] = new Message[]{
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
        };

        queue.add(Priority.MEDIUM, messages[0]);
        queue.add(Priority.LOW, messages[1]);
        queue.add(Priority.HIGHEST, messages[2]);
        queue.add(Priority.LOWEST, messages[3]);
        queue.add(Priority.LOWEST, messages[4]);
        queue.add(Priority.LOW, messages[5]);
        queue.add(Priority.MEDIUM, messages[6]);
        queue.add(Priority.HIGH, messages[7]);
        queue.add(Priority.HIGHEST, messages[8]);
        queue.add(Priority.HIGH, messages[9]);

        assertEquals(queue.removeFirst(), messages[2]);
        assertEquals(queue.removeFirst(), messages[8]);
        assertEquals(queue.removeFirst(), messages[7]);
        assertEquals(queue.removeFirst(), messages[9]);
        assertEquals(queue.removeFirst(), messages[0]);
        assertEquals(queue.removeFirst(), messages[6]);
        assertEquals(queue.removeFirst(), messages[1]);
        assertEquals(queue.removeFirst(), messages[5]);
        assertEquals(queue.removeFirst(), messages[3]);
        assertEquals(queue.removeFirst(), messages[4]);
    }

    @Test
    public void testAddFirst() {
        PrioritySendingQueue queue = new PrioritySendingQueue(1000);

        Message messages[] = new Message[]{
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
        };

        queue.add(Priority.MEDIUM, messages[0]);
        queue.addFirst(Priority.MEDIUM, messages[1]);
        queue.add(Priority.MEDIUM, messages[2]);
        queue.addFirst(Priority.MEDIUM, messages[3]);

        assertEquals(queue.removeFirst(), messages[3]);
        assertEquals(queue.removeFirst(), messages[1]);
        assertEquals(queue.removeFirst(), messages[0]);
        assertEquals(queue.removeFirst(), messages[2]);
    }

    @Test
    public void testMixed() {
        PrioritySendingQueue queue = new PrioritySendingQueue(6);

        Message messages[] = new Message[]{
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
        };

        queue.add(Priority.MEDIUM, messages[0]);
        queue.add(Priority.LOWEST, messages[1]);
        queue.add(Priority.HIGH, messages[2]);
        queue.addFirst(Priority.HIGH, messages[3]);
        queue.add(Priority.HIGH, messages[4]);
        queue.addFirst(Priority.LOWEST, messages[5]);
        queue.addFirst(Priority.MEDIUM, messages[6]);
        queue.add(Priority.HIGH, messages[7]);

        assertEquals(queue.removeFirst(), messages[3]);
        assertEquals(queue.removeFirst(), messages[2]);
        assertEquals(queue.removeFirst(), messages[4]);
        assertEquals(queue.removeFirst(), messages[7]);
        assertEquals(queue.removeFirst(), messages[6]);
        assertEquals(queue.removeFirst(), messages[0]);
        assertEquals(queue.isEmpty(), true);
    }

    @Test
    public void testClear() {
        PrioritySendingQueue queue = new PrioritySendingQueue(1000);

        queue.add(Priority.LOWEST, new DummyMessage(1));
        queue.add(Priority.LOWEST, new DummyMessage(2));
        queue.add(Priority.LOW, new DummyMessage(1));
        queue.add(Priority.LOW, new DummyMessage(2));
        queue.add(Priority.MEDIUM, new DummyMessage(1));
        queue.add(Priority.MEDIUM, new DummyMessage(2));
        queue.add(Priority.HIGH, new DummyMessage(1));
        queue.add(Priority.HIGH, new DummyMessage(2));
        queue.add(Priority.HIGHEST, new DummyMessage(1));
        queue.add(Priority.HIGHEST, new DummyMessage(2));

        assertEquals(queue.isEmpty(), false);
        assertEquals(queue.itemsCount(), 10);
        assertEquals(queue.length(), 15);

        queue.flush();

        assertEquals(queue.isEmpty(), true);
        assertEquals(queue.itemsCount(), 0);
        assertEquals(queue.length(), 0);
    }
}
