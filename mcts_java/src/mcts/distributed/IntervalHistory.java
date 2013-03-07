package mcts.distributed;

public class IntervalHistory {
    int index;
    private long[] history_millis;
    
    public IntervalHistory(int length) {
        index = -1;
        history_millis = new long[length];
    }

    public int length() {
        return history_millis.length;
    }
    
    public void putTime(long interval) {
        index = (index+1)%history_millis.length;
        history_millis[index] = interval;
    }
    
    public double averageInterval() {
        double avg = 0;
        for (int i=0; i<history_millis.length; i++) avg += history_millis[i];
        return avg/history_millis.length;
    }
}
