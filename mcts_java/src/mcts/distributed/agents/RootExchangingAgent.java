package mcts.distributed.agents;

import communication.messages.Message;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.messages.MoveMessage;
import communication.messages.RootMessage;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcts.AvgBackpropagator;
import mcts.GhostsNode;
import mcts.GhostsTree;
import mcts.MCNode;
import mcts.MCTree;
import mcts.MySimulator;
import mcts.PacmanNode;
import mcts.UCBSelector;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.IntervalHistory;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.Pair;

public class RootExchangingAgent extends FullMCTSGhostAgent {   
    private Map<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>> received_roots = new EnumMap<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>>(GHOST.class);    
    private IntervalHistory interval_history = new IntervalHistory(5);
    private long last_message_sending_time = controller.currentVirtualMillis();
     
    private Comparator<Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>> move_strength_entry_comparator = new Comparator<Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>>() {
        @Override
        public int compare(Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t1, Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t2) {
            int i1 = t1.getValue().first;
            int i2 = t2.getValue().first;

            if (i1<i1) {
                return -1;
            } else if (i1>i2) {
                return 1;
            } else {
                int g1 = t1.getValue().second.ordinal();
                int g2 = t2.getValue().second.ordinal();

                if (g1<g2) {
                    return -1;
                } else if (g1>g2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }            
    };
    
    public RootExchangingAgent(DistributedMCTSController controller, final GHOST ghost, int simulation_depth, double ucb_coef, int moves_message_interval, boolean verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
        hookMessageHandler(MoveMessage.class, new MessageHandler() {
            @Override
            public void handleMessage(GhostAgent agent, Message message) {
                RootMessage roots_message = (RootMessage)message;
                received_roots.put(agent.ghost(), roots_message.getRoots());
            }            
        });
    }
    
    public RootExchangingAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, int moves_message_interval) {
        this(controller, ghost, simulation_depth, ucb_coef, moves_message_interval, false);
    }
    
    private Map<EnumMap<GHOST, MOVE>, Long> extractRoot(MCNode subtree) {
        assert(subtree.ghostsOnTurn());
        
        Map<EnumMap<GHOST, MOVE>, Long> root = new HashMap<EnumMap<GHOST, MOVE>, Long>();
        for (MCNode child: subtree.children()) {
            GhostsNode ghost_node = (GhostsNode)child;
            root.put(ghost_node.ghostsMoves().clone(), new Long(ghost_node.visitCount()));
        }
        
        return root;
    }
    
    private EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> extractRoots() {
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);
        
        if (mctree.root().ghostsOnTurn()) {
            roots.put(MOVE.NEUTRAL, extractRoot(mctree.root()));
        } else {
            for (MCNode subtree: mctree.root().children()) {
                PacmanNode pacman_node = (PacmanNode)subtree;
                roots.put(pacman_node.pacmanMove(), extractRoot(subtree));
            }
        }
        
        return roots;
    }
    
    private void sendMessages() {
        long current_time = controller.currentVirtualMillis();
        EnumMap<GHOST, MOVE> best_move = mctree.bestMove(current_game);
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots = extractRoots();
        
        RootMessage message = new RootMessage(roots);
        broadcastMessageIfLowBuffer(message, (long)Math.floor(0.8*interval_history.averageInterval()));
        interval_history.putTime(current_time-last_message_sending_time);
        last_message_sending_time = current_time;
    }

    @Override
    public void step() {
        receiveMessages();
        mctree.iterate();
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        /* Return move with best summed visit count */
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> summed_visit_count = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);
        EnumMap<MOVE, Long> pacman_move_visit_count = new EnumMap<MOVE, Long>(MOVE.class);
        received_roots.put(ghost, extractRoots());
        
        for (EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots: received_roots.values()) {
            for (MOVE pacman_move: roots.keySet()) {
                Map<EnumMap<GHOST, MOVE>, Long> visit_count_map = summed_visit_count.get(pacman_move);
                if (visit_count_map==null) {
                    visit_count_map = new HashMap<EnumMap<GHOST, MOVE>, Long>();
                    summed_visit_count.put(pacman_move, visit_count_map);
                    pacman_move_visit_count.put(pacman_move, new Long(0));
                }
                
                //TODO !!!
            }
        }
        
        
        
        /* Return strongest move (move supported by the most agents)
         * with priority defined by ordering on GHOST enum. */
        EnumMap<GHOST, MOVE> my_best_move = mctree.bestMove(current_game);
        received_moves.put(ghost, new MoveMessage(my_best_move)); /* putTime my current best move to received messages */
        
        Map<EnumMap<GHOST,MOVE>, Pair<Integer, GHOST>> move_strength = new HashMap<EnumMap<GHOST,MOVE>, Pair<Integer, GHOST>>();
        
        for (Entry<GHOST, MoveMessage> entry: received_moves.entrySet()) {
            GHOST g = entry.getKey();
            MoveMessage message = entry.getValue();
            Pair<Integer, GHOST> current_value = move_strength.get(message.moves());
            if (current_value==null) {
                move_strength.put(message.moves(), new Pair<Integer, GHOST>(1, g));
            } else {
                current_value.first++;
            }
        }                
        last_full_move = Collections.min(move_strength.entrySet(), move_strength_entry_comparator).getKey();
        
        if (verbose) {
            System.out.printf(ghost+": ");
            System.out.printf("my best move: %s\n", my_best_move);
            System.out.printf("\tchosen best move: %s\n", last_full_move);
        }
        return last_full_move.get(ghost);
    }

}
