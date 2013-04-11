package mcts.distributed.agents;

import communication.Channel;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.Constants;
import mcts.GuidedSimulator;
import mcts.MCTSControllerStats;
import mcts.MCTSEntity;
import mcts.MCTree;
import mcts.Selector;
import mcts.SimulationsCounter;
import mcts.TreeSimulationsStat;
import mcts.UCBSelector;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.VerboseLevel;

public abstract class GhostAgent implements SimulationsCounter, MCTSEntity, TreeSimulationsStat {
    protected interface MessageHandler {
        void handleMessage(GhostAgent agent, Message message);
    }

    protected final GHOST ghost;
    protected Map<GhostAgent, MessageSender> messageSenders = new HashMap<GhostAgent, MessageSender>();
    protected Map<GhostAgent, MessageReceiver> messageReceivers = new HashMap<GhostAgent, MessageReceiver>();
    protected Random random = new Random(System.currentTimeMillis());
    protected GuidedSimulator mySimulator = new GuidedSimulator(random);
    protected Backpropagator backpropagator = AvgBackpropagator.getInstance();
    protected Selector ucbSelector = new UCBSelector(mySimulator);
    protected double ucbCoef = Constants.DEFAULT_UCB_COEF;
    protected VerboseLevel verboseLevel = VerboseLevel.QUIET;
    protected Map<Class<?>, MessageHandler> messageHandlers = new  HashMap<Class<?>, MessageHandler>();
    protected DistributedMCTSController controller;



    public GhostAgent(DistributedMCTSController controller, GHOST ghost) {
        this.controller = controller;
        this.ghost = ghost;
    }

    public void setRandomSeed(long seed) {
        this.random.setSeed(seed);
    }

    public GHOST ghost() {
        return ghost;
    }

    public String ghostName() {
        return ghost.toString();
    }

    public GhostAgent addAlly(Channel channel, GhostAgent ally) {
        if (!messageSenders.containsKey(ally)) {
            messageSenders.put(ally, channel.sender());
            ally.messageReceivers.put(this, channel.receiver());
        }
        return this;
    }

    public void truncateNetworkBuffers() {
        for (MessageSender sender: messageSenders.values()) {
            sender.channel().clear();
        }
        for (MessageReceiver receiver: messageReceivers.values()) {
            receiver.channel().clear();
        }
    }

    public abstract void updateTree(Game game);
    public abstract MCTree getTree();
    public abstract void step();
    public abstract MOVE getMove();

    protected void hookMessageHandler(Class c, MessageHandler handler) {
        messageHandlers.put(c, handler);
    }

    protected void receiveMessages() {
        for (GhostAgent ally: messageReceivers.keySet()) {
            MessageReceiver receiver = messageReceivers.get(ally);
            if (verboseLevel.check(VerboseLevel.DEBUGGING)) {
                Channel ch = receiver.channel();
                System.out.printf("%s from %s: %s messages (size: %s), transmitting %s (size %s)\n",
                        ghost, ally.ghost, receiver.receiveQueueLength(), receiver.receiveQueueItemsCount(),
                        ch.sendQueueLength(), ch.sendQueueItemsCount());
            }
            while (!receiver.receiveQueueEmpty()) {
                Message message = receiver.receive();
                MessageHandler handler = messageHandlers.get(message.getClass());
                handler.handleMessage(ally, message);
            }
        }
    }

    protected void broadcastMessage(Priority priority, Message message) {
        broadcastMessage(priority, message, false);
    }

    protected void broadcastMessage(Priority priority, Message message, boolean send_first) {
        for (MessageSender sender: messageSenders.values()) {
            if (send_first) {
                sender.sendFirst(priority, message);
            } else {
                sender.send(priority, message);
            }
        }
    }

    protected void broadcastMessageIfLowBuffer(Priority priority, Message message, long sending_interval) {
        for (MessageSender sender: messageSenders.values()) {
            if (sender.secondsToSendAll()<sending_interval) {
                sender.send(priority, message);
            }
        }
    }

    @Override public VerboseLevel getVerboseLevel() { return verboseLevel; }
    @Override public void setVerboseLevel(VerboseLevel verboseLevel) { this.verboseLevel = verboseLevel; }
    @Override public double getUcbCoef() { return ucbCoef; }
    @Override public void setUcbCoef(double ucbCoef) { this.ucbCoef = ucbCoef; }
    @Override public double getDeathWeight() { return this.mySimulator.getMaxDepth(); }
    @Override public void setDeathWeight(double deathWeight) { this.mySimulator.setDeathWeight(deathWeight); }
    @Override public int getSimulationDepth() { return this.mySimulator.getMaxDepth(); }
    @Override public void setSimulationDepth(int simulationDepth) { this.mySimulator.setMaxDepth(simulationDepth); }
    @Override public double getRandomSimulationMoveProbability() { return this.mySimulator.getRandomMoveProb(); }
    @Override public void setRandomSimulationMoveProbability(double randomSimulationMoveProbability) { this.mySimulator.setRandomMoveProb(ucbCoef); }
}
