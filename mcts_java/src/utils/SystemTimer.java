package utils;

public class SystemTimer implements VirtualTimer {
    public final static SystemTimer instance = new SystemTimer();

    private SystemTimer() {}

    @Override
    public long currentVirtualMillis() {
        return System.currentTimeMillis();
    }

}
