package communication;

import communication.messages.Message;
import static org.junit.Assert.*;
import org.junit.Test;
import test_utils.AnotherDummyMessage;
import test_utils.DummyMessage;
import test_utils.MockTimer;
import test_utils.SecondsToSend;
import test_utils.TestUtils;

public class ChannelTest {

    @Test
    public void testSingleMessageTransmission() {
        Network network = new Network(20);
        Channel channel = network.openChannel("channel", 100);

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-3);
        assertEquals(0, channel.transmittedSuccessfully());
        assertEquals(0, channel.transmittedTotal());

        channel.send(Priority.MEDIUM, new DummyMessage(1));
        SecondsToSend sts = new SecondsToSend(1, 20);

        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.002);
        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(1, channel.sendQueueItemsCount());
        assertEquals(1, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(0, channel.transmittedSuccessfully());
        assertEquals(0, channel.transmittedTotal());

        TestUtils.sleep(100);

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(1, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(1, channel.transmittedSuccessfully());
        assertEquals(1, channel.transmittedTotal());

        channel.receive();

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(1, channel.transmittedSuccessfully());
        assertEquals(1, channel.transmittedTotal());
    }

    @Test
    public void testZeroBPS() {
        MockTimer timer = new MockTimer();
        Network network = new Network(0);
        network.setTimer(timer);
        Channel channel = network.openChannel("channel", 100);

        for (int i=0; i<100; i++) {
            channel.send(Priority.HIGHEST, new DummyMessage(1));
        }
        for (int i=0; i<1000; i++) {
            timer.step();
            assertEquals(0, channel.transmittedTotal());
        }
    }

    @Test
    public void testLongMessageTransmission() {
        Network network = new Network(10000);
        Channel channel = network.openChannel("channel", 50000);

        channel.send(Priority.MEDIUM, new DummyMessage(2000));
        SecondsToSend sts = new SecondsToSend(2000, 10000);
        TestUtils.sleep(50);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(1, channel.sendQueueItemsCount());
        assertEquals(1400, channel.sendQueueLength(), 200);
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(0, channel.transmittedSuccessfully());
        assertEquals(0, channel.transmittedTotal());

        TestUtils.sleep(50);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(1, channel.sendQueueItemsCount());
        assertEquals(800, channel.sendQueueLength(), 400);
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(0, channel.transmittedSuccessfully());
        assertEquals(0, channel.transmittedTotal());

        TestUtils.sleep(150);

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(2000, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(2000, channel.transmittedSuccessfully());
        assertEquals(2000, channel.transmittedTotal());

        channel.receive();

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(2000, channel.transmittedSuccessfully());
        assertEquals(2000, channel.transmittedTotal());
    }

    @Test
    public void testClearUnsent() {
        Network network = new Network(20);
        Channel channel = network.openChannel("channel", 100);

        channel.send(Priority.MEDIUM, new DummyMessage(5));
        channel.send(Priority.MEDIUM, new DummyMessage(5));
        channel.send(Priority.MEDIUM, new DummyMessage(5));
        channel.send(Priority.MEDIUM, new DummyMessage(5));
        channel.send(Priority.MEDIUM, new DummyMessage(5));

        channel.flushUnsent();

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(1, channel.sendQueueItemsCount());
        assertEquals(5, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());

        TestUtils.sleep(300);

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(5, channel.receiveQueueLength());
    }

    @Test
    public void testSendQueueFlushUnsentWithClass() {
        Network network = new Network(1);
        Channel channel = network.openChannel("channel", 1000);

        channel.send(Priority.MEDIUM, new DummyMessage(0x01));
        channel.send(Priority.MEDIUM, new AnotherDummyMessage(0x02));
        channel.send(Priority.MEDIUM, new DummyMessage(0x04));
        channel.send(Priority.MEDIUM, new AnotherDummyMessage(0x08));
        channel.send(Priority.MEDIUM, new DummyMessage(0x10));
        channel.send(Priority.MEDIUM, new AnotherDummyMessage(0x20));
        channel.send(Priority.MEDIUM, new AnotherDummyMessage(0x40));
        channel.send(Priority.MEDIUM, new DummyMessage(0x80));
        channel.send(Priority.MEDIUM, new AnotherDummyMessage(0x100));

        channel.sendQueueFlushUnsent(AnotherDummyMessage.class);

        assertEquals(0x95, channel.sendQueueLength());
    }

    @Test
    public void testMultipleMessagesTransmission() {
        Network network = new Network(100);
        Channel channel = network.openChannel("channel", 10000);

        channel.send(Priority.MEDIUM, new DummyMessage(8));
        channel.send(Priority.MEDIUM, new DummyMessage(6));
        channel.send(Priority.MEDIUM, new DummyMessage(6));
        SecondsToSend sts = new SecondsToSend(20, 100);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(3, channel.sendQueueItemsCount());
        assertEquals(20, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(0, channel.transmittedSuccessfully());
        assertEquals(0, channel.transmittedTotal());

        TestUtils.sleep(50);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(3, channel.sendQueueItemsCount());
        assertEquals(15, channel.sendQueueLength(), 10);
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(0, channel.transmittedSuccessfully());
        assertEquals(0, channel.transmittedTotal());

        TestUtils.sleep(50);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(2, channel.sendQueueItemsCount());
        assertEquals(10, channel.sendQueueLength(), 15);
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(8, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(8, channel.transmittedSuccessfully());
        assertEquals(8, channel.transmittedTotal());

        TestUtils.sleep(50);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(1, channel.sendQueueItemsCount());
        assertEquals(5, channel.sendQueueLength(), 20);
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(2, channel.receiveQueueItemsCount());
        assertEquals(14, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(14, channel.transmittedSuccessfully());
        assertEquals(14, channel.transmittedTotal());

        TestUtils.sleep(100);

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(3, channel.receiveQueueItemsCount());
        assertEquals(20, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(20, channel.transmittedSuccessfully());
        assertEquals(20, channel.transmittedTotal());

        channel.receive();

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(2, channel.receiveQueueItemsCount());
        assertEquals(12, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(20, channel.transmittedSuccessfully());
        assertEquals(20, channel.transmittedTotal());

        channel.receive();

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(6, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(20, channel.transmittedSuccessfully());
        assertEquals(20, channel.transmittedTotal());

        channel.receive();

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
        assertEquals(20, channel.transmittedSuccessfully());
        assertEquals(20, channel.transmittedTotal());
    }

    @Test
    public void testMixedTransmittion() {
        Network network = new Network(100);
        Channel channel = network.openChannel("channel", 10000);
        Message messages[] = new Message[]{
            new DummyMessage(10),
            new DummyMessage(10),
            new DummyMessage(20),
            new DummyMessage(10),
            new DummyMessage(10),
        };

        channel.send(Priority.MEDIUM, messages[0]);
        channel.send(Priority.HIGH, messages[1]);
        SecondsToSend sts = new SecondsToSend(20, 100);

        TestUtils.sleep(150);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(1, channel.sendQueueItemsCount());
        assertEquals(sts.bytes(), channel.sendQueueLength(), 2);
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(10, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(10, channel.transmittedSuccessfully());
        assertEquals(10, channel.transmittedTotal());

        assertEquals(messages[0], channel.receive());

        channel.send(Priority.HIGH, messages[2]);
        channel.send(Priority.HIGHEST, messages[3]);
        sts.addBytes(30);

        TestUtils.sleep(100);

        assertEquals(false, channel.sendQueueEmpty());
        assertEquals(2, channel.sendQueueItemsCount());
        assertEquals(sts.bytes(), channel.sendQueueLength(), 2);
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(1, channel.receiveQueueItemsCount());
        assertEquals(10, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(20, channel.transmittedSuccessfully());
        assertEquals(20, channel.transmittedTotal());

        channel.sendFirst(Priority.HIGH, messages[4]);
        sts.addBytes(10);

        TestUtils.sleep(400);

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(sts.bytes(), channel.sendQueueLength(), 2);
        assertEquals(false, channel.receiveQueueEmpty());
        assertEquals(4, channel.receiveQueueItemsCount());
        assertEquals(50, channel.receiveQueueLength());
        assertEquals(sts.remaining(), channel.secondsToSendAll(), 0.003);
        assertEquals(60, channel.transmittedSuccessfully());
        assertEquals(60, channel.transmittedTotal());

        assertEquals(messages[1], channel.receive());
        assertEquals(messages[3], channel.receive());
        assertEquals(messages[4], channel.receive());
        assertEquals(messages[2], channel.receive());

        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(60, channel.transmittedSuccessfully());
        assertEquals(60, channel.transmittedTotal());
    }

    @Test
    public void testMessagePriority() {
        Network network = new Network(500);
        Channel channel = network.openChannel("channel", 1000);
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

        channel.send(Priority.HIGH, messages[0]);
        channel.send(Priority.LOW, messages[1]);
        channel.send(Priority.LOWEST, messages[2]);
        channel.send(Priority.HIGH, messages[3]);
        channel.send(Priority.MEDIUM, messages[4]);
        channel.send(Priority.LOWEST, messages[5]);
        channel.send(Priority.HIGHEST, messages[6]);
        channel.send(Priority.HIGHEST, messages[7]);
        channel.send(Priority.MEDIUM, messages[8]);
        channel.send(Priority.LOW, messages[9]);

        TestUtils.sleep(50);

        assertEquals(messages[0], channel.receive());
        assertEquals(messages[6], channel.receive());
        assertEquals(messages[7], channel.receive());
        assertEquals(messages[3], channel.receive());
        assertEquals(messages[4], channel.receive());
        assertEquals(messages[8], channel.receive());
        assertEquals(messages[1], channel.receive());
        assertEquals(messages[9], channel.receive());
        assertEquals(messages[2], channel.receive());
        assertEquals(messages[5], channel.receive());
    }

    @Test
    public void testSendFirst() {
        Network network = new Network(200);
        Channel channel = network.openChannel("channel", 1000);
        Message messages[] = new Message[]{
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
            new DummyMessage(1),
        };

        channel.send(Priority.MEDIUM, messages[0]);
        channel.sendFirst(Priority.MEDIUM, messages[1]);
        channel.send(Priority.MEDIUM, messages[2]);
        channel.sendFirst(Priority.MEDIUM, messages[3]);

        TestUtils.sleep(50);

        assertEquals(messages[0], channel.receive());
        assertEquals(messages[3], channel.receive());
        assertEquals(messages[1], channel.receive());
        assertEquals(messages[2], channel.receive());
    }

    @Test
    public void testBufferSize() {
        Network network = new Network(100);
        Channel channel = network.openChannel("channel", 1000);

        channel.send(Priority.MEDIUM, new DummyMessage(300));
        channel.send(Priority.MEDIUM, new DummyMessage(300));
        channel.send(Priority.MEDIUM, new DummyMessage(300));
        channel.send(Priority.MEDIUM, new DummyMessage(300));
        channel.send(Priority.MEDIUM, new DummyMessage(300));
        assertEquals(4, channel.sendQueueItemsCount());

        channel.send(Priority.MEDIUM, new DummyMessage(100));
        assertEquals(5, channel.sendQueueItemsCount());
    }

    @Test
    public void testTransmissionSpeed() {
        Network network1 = new Network(100);
        Network network2 = new Network(1000);
        Network network3 = new Network(35000);
        Channel channel1a = network1.openChannel("channel1a", 1000);
        Channel channel1b = network1.openChannel("channel1b", 1000);
        Channel channel2 = network2.openChannel("channel2", 1000);
        Channel channel3 = network3.openChannel("channel3", 1000);

        assertEquals(channel1a.transmissionSpeed(), 100);
        assertEquals(channel1b.transmissionSpeed(), 100);
        assertEquals(channel2.transmissionSpeed(), 1000);
        assertEquals(channel3.transmissionSpeed(), 35000);
    }

    @Test
    public void testSenderReceiverChannel() {
        Network network = new Network(1000);
        Channel channel = network.openChannel("channel", 1000);

        assertEquals(channel.sender(), (MessageSender)channel);
        assertEquals(channel.receiver(), (MessageReceiver)channel);
        assertEquals(channel.channel(), channel);
    }

    @Test
    public void testName() {
        final String zlutoucky_kun = "Žluťoučký kůň úpěl ďábelské ódy";
        Network network = new Network(100);
        Channel channel1 = network.openChannel("channel1", 1000);
        Channel channel2 = network.openChannel(zlutoucky_kun, 1000);

        assertEquals(channel1.name(), "channel1");
        assertEquals(channel2.name(), zlutoucky_kun);
    }

    @Test
    public void testClear() {
        Network network = new Network(100);
        Channel channel = network.openChannel("channel", 1000);

        channel.send(Priority.MEDIUM, new DummyMessage(2));
        channel.send(Priority.MEDIUM, new DummyMessage(2));
        channel.send(Priority.MEDIUM, new DummyMessage(2));
        channel.send(Priority.MEDIUM, new DummyMessage(2));
        channel.send(Priority.MEDIUM, new DummyMessage(2));

        TestUtils.sleep(50);

        channel.flush();

        assertEquals(true, channel.sendQueueEmpty());
        assertEquals(0, channel.sendQueueItemsCount());
        assertEquals(0, channel.sendQueueLength());
        assertEquals(true, channel.receiveQueueEmpty());
        assertEquals(0, channel.receiveQueueItemsCount());
        assertEquals(0, channel.receiveQueueLength());
        assertEquals(0, channel.secondsToSendAll(), 1e-6);
    }
}
