package mcts.distributed.agents;

import communication.messages.Message;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.Channel;
import communication.Network;
import communication.messages.MoveMessage;
import java.util.HashMap;
import java.util.Map;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.MySimulator;
import mcts.Selector;
import mcts.UCBSelector;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public abstract class GhostAgent {
    protected interface MessageHandler {
        void handleMessage(GhostAgent agent, Message message);
    }
    protected final GHOST ghost;
    protected Map<GhostAgent, MessageSender> message_senders = new HashMap<GhostAgent, MessageSender>();
    protected Map<GhostAgent, MessageReceiver> message_receivers = new HashMap<GhostAgent, MessageReceiver>();
    protected MySimulator my_simulator;
    protected Backpropagator backpropagator;
    protected Selector ucb_selector;
    protected double ucb_coef;
    protected boolean verbose;
    protected Map<Class<?>, MessageHandler> message_handlers = new  HashMap<Class<?>, MessageHandler>();
    protected DistributedMCTSController controller;
     
    
    public GhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, boolean verbose) {
        this.controller = controller;
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
    
    public GhostAgent addAlly(Channel channel, GhostAgent ally) {
        if (!message_senders.containsKey(ally)) {
            message_senders.put(ally, channel.sender());
            ally.message_receivers.put(this, channel.receiver());
        }
        return this;
    }
    
    public abstract void updateTree(Game game);
    public abstract void step();
    public abstract MOVE getMove();
    
    protected void hookMessageHandler(Class c, MessageHandler handler) {
        message_handlers.put(c, handler);
    }
    
    protected void receiveMessages() {   
        for (GhostAgent ally: message_receivers.keySet()) {
            MessageReceiver receiver = message_receivers.get(ally);
            while (!receiver.receiveQueueEmpty()) {
                Message message = receiver.receive();
                MessageHandler handler = message_handlers.get(message.getClass());
                handler.handleMessage(ally, message);                
            }
        }        
    }
    
    protected void broadcastMessage(Message message) {
        for (MessageSender sender: message_senders.values()) {
            sender.send(message);
        }
    }
    
    protected void broadcastMessageIfLowBuffer(Message message, long sending_interval) {
        for (MessageSender sender: message_senders.values()) {
            if (sender.secondsToSendAll()<sending_interval) {
                sender.send(message);
            }
        }
    }
}
