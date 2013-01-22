package mcts.distributed;

import communication.Message;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.P2PChannel;
import communication.P2PNetwork;
import java.util.HashMap;
import java.util.Map;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.MySimulator;
import mcts.Selector;
import mcts.UCBSelector;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public abstract class GhostAgent<M extends Message> {
    protected GHOST ghost;
    protected Map<GhostAgent, MessageSender<M>> message_senders = new HashMap<GhostAgent, MessageSender<M>>();
    protected Map<GhostAgent, MessageReceiver<M>> message_receivers = new HashMap<GhostAgent, MessageReceiver<M>>();
    protected MySimulator my_simulator;
    protected Backpropagator backpropagator;
    protected Selector ucb_selector;
    protected double ucb_coef;
    protected boolean verbose;
     
    
    public GhostAgent(GHOST ghost, int simulation_depth, double ucb_coef, boolean verbose) {
        this.ghost = ghost;
        this.my_simulator = new MySimulator(simulation_depth);        
        this.ucb_selector = new UCBSelector(30, my_simulator);
        this.backpropagator = AvgBackpropagator.getInstance();
        this.ucb_coef = ucb_coef;
        this.verbose = verbose;
    }
    
    public GHOST ghost() {
        return ghost;
    }
    
    public String ghostName() {
        return ghost.toString();
    }
    
    public GhostAgent addAlly(P2PChannel<M> channel, GhostAgent ally) {
        if (!message_senders.containsKey(ally)) {
            message_senders.put(ally, channel.sender());
            ally.message_receivers.put(this, channel.receiver());
        }
        return this;
    }
    
    public abstract void updateTree(Game game);
    public abstract void step();
    public abstract MOVE getMove();
}
