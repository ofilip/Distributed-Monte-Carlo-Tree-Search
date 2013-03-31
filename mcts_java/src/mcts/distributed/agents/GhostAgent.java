package mcts.distributed.agents;

import communication.Channel;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import java.util.HashMap;
import java.util.Map;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.GuidedSimulator;
import mcts.MCTree;
import mcts.Selector;
import mcts.SimulationsCounter;
import mcts.UCBSelector;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.VerboseLevel;

public abstract class GhostAgent implements SimulationsCounter {
    protected interface MessageHandler {
        void handleMessage(GhostAgent agent, Message message);
    }

    protected final GHOST ghost;
    protected Map<GhostAgent, MessageSender> message_senders = new HashMap<GhostAgent, MessageSender>();
    protected Map<GhostAgent, MessageReceiver> message_receivers = new HashMap<GhostAgent, MessageReceiver>();
    protected GuidedSimulator my_simulator;
    protected Backpropagator backpropagator;
    protected Selector ucb_selector;
    protected double ucb_coef;
    protected VerboseLevel verbose;
    protected Map<Class<?>, MessageHandler> message_handlers = new  HashMap<Class<?>, MessageHandler>();
    protected DistributedMCTSController controller;


    public GhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        this.controller = controller;
        this.ghost = ghost;
        this.my_simulator = new GuidedSimulator(simulation_depth, System.currentTimeMillis()+ghost.ordinal(), GuidedSimulator.DEFAULT_RANDOM_MOVE_PROB, GuidedSimulator.DEFAULT_DEATH_WEIGHT);
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

    public void truncateNetworkBuffers() {
        for (MessageSender sender: message_senders.values()) {
            sender.channel().clear();
        }
        for (MessageReceiver receiver: message_receivers.values()) {
            receiver.channel().clear();
        }
    }

    public abstract void updateTree(Game game);
    public abstract MCTree getTree();
    public abstract void step();
    public abstract MOVE getMove();

    protected void hookMessageHandler(Class c, MessageHandler handler) {
        message_handlers.put(c, handler);
    }

    protected void receiveMessages() {
        for (GhostAgent ally: message_receivers.keySet()) {
            MessageReceiver receiver = message_receivers.get(ally);
            if (verbose.check(VerboseLevel.DEBUGGING)) {
                Channel ch = receiver.channel();
                System.out.printf("%s from %s: %s messages (size: %s), transmitting %s (size %s)\n",
                        ghost, ally.ghost, receiver.receiveQueueLength(), receiver.receiveQueueItemsCount(),
                        ch.sendQueueLength(), ch.sendQueueItemsCount());
            }
            while (!receiver.receiveQueueEmpty()) {
                Message message = receiver.receive();
                MessageHandler handler = message_handlers.get(message.getClass());
                handler.handleMessage(ally, message);
            }
        }
    }

    protected void broadcastMessage(Priority priority, Message message) {
        broadcastMessage(priority, message, false);
    }

    protected void broadcastMessage(Priority priority, Message message, boolean send_first) {
        for (MessageSender sender: message_senders.values()) {
            if (send_first) {
                sender.sendFirst(priority, message);
            } else {
                sender.send(priority, message);
            }
        }
    }

    protected void broadcastMessageIfLowBuffer(Priority priority, Message message, long sending_interval) {
        for (MessageSender sender: message_senders.values()) {
            if (sender.secondsToSendAll()<sending_interval) {
                sender.send(priority, message);
            }
        }
    }
}
