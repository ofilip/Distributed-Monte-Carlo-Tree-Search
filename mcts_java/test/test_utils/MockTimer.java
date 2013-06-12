package test_utils;

import utils.VirtualTimer;

public class MockTimer implements VirtualTimer {
    private long millis = 0;
    public void step() { millis++; }
    public long currentVirtualMillis() {
        return millis;
    }
}