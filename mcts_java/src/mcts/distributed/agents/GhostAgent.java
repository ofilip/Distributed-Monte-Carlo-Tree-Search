package mcts.distributed.agents;

import communication.Channel;
import communication.MessageCallback;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import mcts.AvgBackpropagator;
import mcts.Backpropagator;
import mcts.Constants;
import mcts.GuidedSimulator;
import mcts.MCTSController;
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

public abstract class GhostAgent implements SimulationsCounter, MCTSEntity, TreeSimulationsStat, Runnable {
    protected interface MessageHandler {
        void handleMessage(GhostAgent agent, Message message);
    }

    protected final GHOST ghost;
    protected Map<GhostAgent, MessageSender> messageSenders = new HashMap<GhostAgent, MessageSender>();
    protected Map<GhostAgent, MessageReceiver> messageReceivers = new HashMap<GhostAgent, MessageReceiver>();
    protected long randomSeed = System.currentTimeMillis();
    protected Random random = new Random(randomSeed);
    protected GuidedSimulator mySimulator = new GuidedSimulator(random);
    protected Backpropagator backpropagator = AvgBackpropagator.getInstance();
    protected Selector ucbSelector = new UCBSelector(mySimulator);
    protected double ucbCoef = Constants.DEFAULT_UCB_COEF;
    protected VerboseLevel verboseLevel = VerboseLevel.QUIET;
    protected Map<Class<?>, MessageHandler> messageHandlers = new  HashMap<Class<?>, MessageHandler>();
    protected DistributedMCTSController controller;
    protected boolean equalRandomSeed = false;

    /* Thread data */
    protected long timeDue;
    protected Game currentGame;


    public boolean getEqualRandomSeed() { return equalRandomSeed; }
    public void setEqualRandomSeed(boolean equalRandomSeed) { this.equalRandomSeed = equalRandomSeed; }

    public GhostAgent(DistributedMCTSController controller, GHOST ghost) {
        this.controller = controller;
        this.ghost = ghost;
    }

    public void setRandomSeed(long seed) {
        randomSeed = seed;
        this.random.setSeed(seed);
    }

    public Random getRandom() {
        return random;
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
            sender.channel().flush();
        }
        for (MessageReceiver receiver: messageReceivers.values()) {
            receiver.channel().flush();
        }
    }

    public void putThreadData(Game currentGame, long timeDue) {
        this.currentGame = currentGame.copy();
        this.timeDue = timeDue;
    }

    @Override
    public void run() {
        truncateNetworkBuffers();
        updateTree(currentGame);
        while (System.currentTimeMillis()<timeDue) {
            step();
        }
    }

    public abstract void updateTree(Game game);
    public abstract MCTree getTree();
    public abstract void step();
    public abstract MOVE getMove();
    public abstract EnumMap<GHOST,MOVE> getFullMove();

    protected void hookMessageHandler(Class c, MessageHandler handler) {
        messageHandlers.put(c, handler);
    }

    protected void receiveMessages() {
        for (GhostAgent ally: messageReceivers.keySet()) {
            MessageReceiver receiver = messageReceivers.get(ally);
//            if (verboseLevel.check(VerboseLevel.DEBUGGING)) {
//                Channel ch = receiver.channel();
//                System.out.printf("%s from %s: %s messages (size: %s), transmitting %s (size %s)\n",
//                        ghost, ally.ghost, receiver.receiveQueueLength(), receiver.receiveQueueItemsCount(),
//                        ch.sendQueueLength(), ch.sendQueueItemsCount());
//            }
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

    protected void flushMessages(Class messageClass) {
        for (MessageSender sender: messageSenders.values()) {
            sender.sendQueueFlushUnsent(messageClass);
        }
    }

    protected void broadcastMessage(Priority priority, Message message, boolean sendFirst) {
        message.onMessageDropped(new MessageCallback() {
            public void call(Message message) {
//                System.err.printf("[%s:%s] Message dropped: %s\n", ghost, controller.currentVirtualMillis(), message.toString());
            }
        });
        for (MessageSender sender: messageSenders.values()) {
            if (sendFirst) {
                sender.sendFirst(priority, message);
            } else {
                sender.send(priority, message);
            }
        }
    }

//    protected void broadcastMessageIfLowBuffer(Priority priority, Message message, long sendingInterval) {
//        for (MessageSender sender: messageSenders.values()) {
//            if (sender.secondsToSendAll()<sendingInterval) {
//                sender.send(priority, message);
//            }
//        }
//    }

    public abstract long calculatedSimulations();
    public abstract long totalSimulations();
    @Override public VerboseLevel getVerboseLevel() { return verboseLevel; }
    @Override public void setVerboseLevel(VerboseLevel verboseLevel) { this.verboseLevel = verboseLevel; }
    @Override public double getUcbCoef() { return ucbCoef; }
    @Override public void setUcbCoef(double ucbCoef) { this.ucbCoef = ucbCoef; }
    @Override public double getDeathWeight() { return this.mySimulator.getDeathWeight(); }
    @Override public void setDeathWeight(double deathWeight) { this.mySimulator.setDeathWeight(deathWeight); }
    @Override public int getSimulationDepth() { return this.mySimulator.getMaxDepth(); }
    @Override public void setSimulationDepth(int simulationDepth) { this.mySimulator.setMaxDepth(simulationDepth); }
    @Override public double getRandomSimulationMoveProbability() { return this.mySimulator.getRandomMoveProb(); }
    @Override public void setRandomSimulationMoveProbability(double randomSimulationMoveProbability) { this.mySimulator.setRandomMoveProb(randomSimulationMoveProbability); }
}
