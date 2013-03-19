package communication;

import communication.messages.Message;
import java.util.Random;
import utils.SystemTimer;
import utils.VirtualTimer;

public class HMMReliability implements Reliability { 
    private boolean reliable = true;
    private double r_reliability;
    private double u_reliability;    
    private double ru_prob;
    private double ur_prob;
    private long millis_transition;
    private VirtualTimer timer;
    private long last_transition;
    private Random random = new Random(System.currentTimeMillis());
    
    public HMMReliability(double r_reliability, double u_reliability, double ru_prob, double ur_prob, long millis_transition) {
        this(r_reliability, u_reliability, ru_prob, ur_prob, millis_transition, SystemTimer.instance);
    }
    
    public HMMReliability(double r_reliability, double u_reliability, double ru_prob, double ur_prob, long millis_transition, VirtualTimer timer) {
        this.r_reliability = r_reliability;
        this.u_reliability = u_reliability;
        this.ru_prob = ru_prob;
        this.ur_prob = ur_prob;
        this.millis_transition = millis_transition;
        this.timer = timer;
        this.last_transition = timer.currentMillis();
    }
    
    @Override
    public boolean isTransmitted(Message message) {
        long now = timer.currentMillis();
        
        /* process transitions */
        while (last_transition+millis_transition<now) {
            if (reliable) {
                if (random.nextDouble()<ru_prob) {
                    reliable = false;
                }
            } else {
                if (random.nextDouble()<ur_prob) {
                    reliable = true;
                }                
            }
            last_transition += millis_transition;
        }
        
        /* transmit */
        if (reliable) {
            return random.nextDouble()<r_reliability;
        } else {
            return random.nextDouble()<u_reliability;
        }
    }
    
    @Override
    public HMMReliability clone() {
        return new HMMReliability(r_reliability, u_reliability, ru_prob, ur_prob, millis_transition, timer);
    }
}
