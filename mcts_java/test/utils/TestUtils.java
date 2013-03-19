package utils;

import static org.junit.Assert.*;

public class TestUtils {    
    static public long sleep(int millis) {
        long now = System.currentTimeMillis();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            fail("InterrupedException caught: "+ex.getMessage());
        }
        return System.currentTimeMillis()-now;
    }
    
    static public void todo() {
        fail("TODO");
    }
}
