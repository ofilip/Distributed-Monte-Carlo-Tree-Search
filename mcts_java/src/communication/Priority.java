package communication;

import java.util.ArrayList;
import java.util.List;

public enum Priority {
    HIGHEST,
    HIGH,
    MEDIUM,
    LOW,
    LOWEST;
    
    public final static List<Priority> highest2lowest = new ArrayList<Priority>();
    static {
        highest2lowest.add(HIGHEST);
        highest2lowest.add(HIGH);
        highest2lowest.add(MEDIUM);
        highest2lowest.add(LOW);
        highest2lowest.add(LOWEST);
    }
    public final static List<Priority> lowest2highest = new ArrayList<Priority>();
    static {
        lowest2highest.add(LOWEST);
        lowest2highest.add(LOW);
        lowest2highest.add(MEDIUM);
        lowest2highest.add(HIGH);
        lowest2highest.add(HIGHEST);
    }
}
