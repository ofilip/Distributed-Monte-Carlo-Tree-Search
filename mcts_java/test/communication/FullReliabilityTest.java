/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import communication.messages.Message;
import org.junit.Test;
import static org.junit.Assert.*;
import utils.DummyMessage;

public class FullReliabilityTest {
    @Test
    public void testIsTransmitted() {
        assertEquals(true, new FullReliability().isTransmitted(new DummyMessage(100)));
    }
}
