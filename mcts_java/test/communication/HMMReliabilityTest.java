/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import utils.DummyMessage;
import utils.VirtualTimer;

class MockRandom extends Random {
    private int i = 0;
    private double[] vals = new double[]{};

    public MockRandom(final double[] vals) {
        this.vals = vals;
    }

    @Override
    public double nextDouble() {
        double res = vals[i];
        i = (i+1)%vals.length;
        return res;
    }
}

class MockTimer implements VirtualTimer {
    private long millis = 0;
    public void step() { millis++; }
    public long currentVirtualMillis() {
        return millis;
    }
};

public class HMMReliabilityTest {
    void assignRandom(HMMReliability reliability, Random random) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        reliability.random = random;
    }

    boolean isReliable(HMMReliability reliability) throws IllegalArgumentException, NoSuchFieldException, NoSuchFieldException, IllegalAccessException {
        return reliability.reliable;
    }

    @Test
    public void hmmReliabilityTest() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Random rnd = new MockRandom(new double[]{
            0.899, //0
            0.021, //1
            0.901,
            0.019, //2
            0.101,
            0.099,
            0.01001, //3
            0.05,
            0.0099, //4
            0.5,
            0.1999, //5
            0.011,
            0.0099,
            0.5
        });
        MockTimer timer = new MockTimer();

        HMMReliability rel = new HMMReliability(0.9, 0.1, 0.02, 0.01, 1, timer);
        assignRandom(rel, rnd);
        assertEquals(0.9, rel.getRReliability(), 0.001);
        assertEquals(0.1, rel.getUReliability(), 0.001);
        assertEquals(0.02, rel.getRuProb(), 0.001);
        assertEquals(0.01, rel.getUrProb(), 0.001);

        assertTrue(isReliable(rel));
        assertTrue(rel.isTransmitted(null)); //0
        timer.step();
        assertFalse(rel.isTransmitted(null)); //1
        assertTrue(isReliable(rel));
        timer.step();
        assertFalse(rel.isTransmitted(null)); //2
        assertTrue(rel.isTransmitted(null));
        assertFalse(isReliable(rel));
        timer.step();
        assertTrue(rel.isTransmitted(null)); //3
        assertFalse(isReliable(rel));
        timer.step();
        assertTrue(rel.isTransmitted(null)); //4
        assertTrue(isReliable(rel));
        timer.step();
        timer.step();
        timer.step();
        assertTrue(rel.isTransmitted(null)); //5
        assertTrue(isReliable(rel));

    }

    @Test
    public void integrationTest() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Random rnd = new MockRandom(new double[]{
            0.021, 0.89,
            0.021, 0.91,
            0.019, 0.101,
            0.0101, 0.099,
            0.0099, 0.89
        });
        MockTimer timer = new MockTimer();
        Network network = new Network(1000);
        network.setTimer(timer);
        HMMReliability rel = new HMMReliability(0.9, 0.1, 0.02, 0.01, 1, timer);
        assignRandom(rel, rnd);
        network.setReliability(rel);
        Channel ch = network.openChannel("ch", 100000);

        ch.send(Priority.HIGHEST, new DummyMessage(1));
        timer.step();
        ch.receive();
        assertEquals(1, ch.transmittedSuccessfully());

        ch.send(Priority.HIGHEST, new DummyMessage(1));
        timer.step();
        ch.receive();
        assertEquals(1, ch.transmittedSuccessfully());

        ch.send(Priority.HIGHEST, new DummyMessage(1));
        timer.step();
        ch.receive();
        assertEquals(1, ch.transmittedSuccessfully());

        ch.send(Priority.HIGHEST, new DummyMessage(1));
        timer.step();
        ch.receive();
        assertEquals(2, ch.transmittedSuccessfully());

        ch.send(Priority.HIGHEST, new DummyMessage(1));
        timer.step();
        ch.receive();
        assertEquals(3, ch.transmittedSuccessfully());
    }
}
