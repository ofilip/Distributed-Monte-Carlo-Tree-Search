package communication;

public class FullReliability implements Reliability {
    private static FullReliability instance = null;
    
    @Override
    public boolean isTransmitted(Message message) {
        return true;
    }
    
    private FullReliability() {}
    
    public static FullReliability getInstance() {
        if (instance==null) {
            instance = new FullReliability();
        }
        
        return instance;
    }

}
