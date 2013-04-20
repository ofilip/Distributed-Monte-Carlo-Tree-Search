package utils;

public class SecondsToSend {
    long start_time;
    long start_bytes;
    long bytes_per_second;

    public SecondsToSend(long start_bytes, long bytes_per_second) {
        this.start_time = System.currentTimeMillis();
        this.start_bytes = start_bytes;
        this.bytes_per_second = bytes_per_second;
    }

    public void addBytes(long bytes) {
        if (remaining()==0) {
            start_time = System.currentTimeMillis();
            start_bytes = 0;
        }
        start_bytes += bytes;
    }

    private double secondsPassed() { return 0.001*(System.currentTimeMillis()-start_time); }

    public double remaining() {
        return Math.max(0, start_bytes/(double)bytes_per_second - secondsPassed());
    }

    public long bytes() {
        return (long)Math.max(0, Math.ceil(start_bytes - secondsPassed()*bytes_per_second));
    }
}
