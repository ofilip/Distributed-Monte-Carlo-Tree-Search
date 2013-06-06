package communication;

import communication.messages.Message;
import java.util.Random;
import utils.SystemTimer;
import utils.VirtualTimer;

public class HMMReliability implements Reliability {
    private double r_reliability;
    private double u_reliability;
    private double ru_prob;
    private double ur_prob;
    private long millis_transition;
    private VirtualTimer timer;
    private long last_transition;

    //XXX: public for testing purposes only...
    public boolean reliable = true;

    public Random random = new Random(System.currentTimeMillis());

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
        this.last_transition = timer.currentVirtualMillis();
    }

    @Override
    public boolean isTransmitted(Message message) {
        long now = timer.currentVirtualMillis();

        /* process transitions */
        while (last_transition+getMillisTransition()<=now) {
            double nextDouble = random.nextDouble();
            if (reliable) {
                if (nextDouble<getRuProb()) {
                    reliable = false;
                }
            } else {
                if (nextDouble<getUrProb()) {
                    reliable = true;
                }
            }
            last_transition += getMillisTransition();
        }

        /* transmit */
        double nextDouble = random.nextDouble();
        if (reliable) {
            return nextDouble<getRReliability();
        } else {
            return nextDouble<getUReliability();
        }
    }



    @Override
    public HMMReliability clone() {
        HMMReliability clone =  new HMMReliability(getRReliability(), getUReliability(), getRuProb(), getUrProb(), getMillisTransition(), timer);
        clone.random = random;
        return clone;
    }

    /**
     * @return the r_reliability
     */
    public double getRReliability() {
        return r_reliability;
    }

    /**
     * @return the u_reliability
     */
    public double getUReliability() {
        return u_reliability;
    }

    /**
     * @return the ru_prob
     */
    public double getRuProb() {
        return ru_prob;
    }

    /**
     * @return the ur_prob
     */
    public double getUrProb() {
        return ur_prob;
    }

    /**
     * @return the millis_transition
     */
    public long getMillisTransition() {
        return millis_transition;
    }
}
