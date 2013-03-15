package mcts.distributed.agents;

import communication.messages.Message;
import communication.messages.RootMessage;
import communication.messages.SimulationResultMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mcts.Action;
import mcts.GhostsNode;
import mcts.MCNode;
import mcts.PacmanNode;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.IntervalHistory;
import mcts.exceptions.InvalidActionListException;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;
import utils.VerboseLevel;

public class SimulationResultsPassingAgent extends FullMCTSGhostAgent {   
//    private Map<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>> received_roots = new EnumMap<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>>(GHOST.class);    
//    private IntervalHistory interval_history = new IntervalHistory(5);
//    private long last_message_sending_time = 0;
     
//    private Comparator<Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>> move_strength_entry_comparator = new Comparator<Entry<EnumMap<GHOST,MOVE>, Pair<Integer,GHOST>>>() {
//        @Override
//        public int compare(Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t1, Entry<EnumMap<GHOST, MOVE>, Pair<Integer, GHOST>> t2) {
//            int i1 = t1.getValue().first;
//            int i2 = t2.getValue().first;
//
//            if (i1<i1) {
//                return -1;
//            } else if (i1>i2) {
//                return 1;
//            } else {
//                int g1 = t1.getValue().second.ordinal();
//                int g2 = t2.getValue().second.ordinal();
//
//                if (g1<g2) {
//                    return -1;
//                } else if (g1>g2) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            }
//        }            
//    };
    
    public SimulationResultsPassingAgent(DistributedMCTSController controller, final GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
        
        hookMessageHandler(SimulationResultMessage.class, new MessageHandler() {
            @Override public void handleMessage(GhostAgent agent, Message message) {
//                SimulationResultMessage result_message = (SimulationResultMessage)message;
//                try {
//                    mctree.applySimulationResult(result_message.treeMoves(), result_message.simulationResult());
//                } catch (InvalidActionListException e) { assert(false); }
            }
        });
    }
    
    public SimulationResultsPassingAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef) {
        this(controller, ghost, simulation_depth, ucb_coef, VerboseLevel.QUIET);
    }
    
//    private Map<EnumMap<GHOST, MOVE>, Long> extractRoot(MCNode subtree) {
//        assert(subtree.ghostsOnTurn());
//        
//        Map<EnumMap<GHOST, MOVE>, Long> root = new HashMap<EnumMap<GHOST, MOVE>, Long>();
//        for (MCNode child: subtree.children()) {
//            GhostsNode ghost_node = (GhostsNode)child;
//            root.put(ghost_node.ghostsMoves().clone(), new Long(ghost_node.visitCount()));
//        }
//        
//        return root;
//    }
    
//    private EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> extractRoots() {
//        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);
//        
//        if (mctree.root().ghostsOnTurn()) {
//            roots.put(MOVE.NEUTRAL, extractRoot(mctree.root()));
//        } else if (mctree.root().halfstep()){
//            for (MCNode subtree: mctree.root().children()) {
//                if (subtree.pacmanOnTurn()) {
//                    return null;
//                }
//                PacmanNode pacman_node = (PacmanNode)subtree;
//                roots.put(pacman_node.pacmanMove(), extractRoot(subtree));
//            }
//        } else return null;
//        
//        return roots;
//    }
    
    private void sendMessages(List<Action> action_list, double simulation_result) {
        //TODO:
        // * aggregated information passing
        // * load ballancing
        // * move message (send prioritized)
        // * push message at the beginning of messge queue
        SimulationResultMessage message = new SimulationResultMessage(action_list, simulation_result);
        broadcastMessage(message);
    }

    @Override
    public void step() {
        List<Action> action_list = new ArrayList<Action>();
        
        receiveMessages();
        double simulation_result = mctree.iterate(action_list);
        sendMessages(action_list, simulation_result);
    }

    @Override
    public MOVE getMove() {
        //TODO
        //return null;
        return mctree.bestMove(current_game).get(ghost);
        
//        /* Return move with best summed visit count */
//        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> summed_visit_count = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);
//        EnumMap<MOVE, Long> pacman_move_visit_count = new EnumMap<MOVE, Long>(MOVE.class);
//        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> my_roots = extractRoots();
//        
//        if (my_roots!=null) {
//            received_roots.put(ghost, my_roots);
//        }
//        
//        for (EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots: received_roots.values()) {
//            for (MOVE pacman_move: roots.keySet()) {
//                Map<EnumMap<GHOST, MOVE>, Long> root = roots.get(pacman_move);
//                Map<EnumMap<GHOST, MOVE>, Long> visit_count_map = summed_visit_count.get(pacman_move);
//                                
//                if (visit_count_map==null) {
//                    visit_count_map = new HashMap<EnumMap<GHOST, MOVE>, Long>();
//                    summed_visit_count.put(pacman_move, visit_count_map);
//                    pacman_move_visit_count.put(pacman_move, new Long(0));
//                }
//                
//                for (EnumMap<GHOST, MOVE> ghost_move: root.keySet()) {
//                    Long sum = visit_count_map.get(ghost_move);
//                    Long pacman_sum = pacman_move_visit_count.get(pacman_move);
//                    Long count = root.get(ghost_move);
//                    
//                    if (pacman_sum==null) pacman_sum = new Long(0);
//                    if (sum==null) sum = new Long(0);
//                    visit_count_map.put(ghost_move, sum+count);
//                    pacman_move_visit_count.put(pacman_move, pacman_sum+count);
//                }
//            }
//        }
//        
//        MOVE best_pacman_move = null;
//        
//        if (pacman_move_visit_count.isEmpty()) {
//            return MOVE.NEUTRAL;
//        }
//        
//        for (MOVE pacman_move: pacman_move_visit_count.keySet()) {
//            if (best_pacman_move==null||pacman_move_visit_count.get(pacman_move)>pacman_move_visit_count.get(best_pacman_move)) {
//                best_pacman_move = pacman_move;
//            }
//        }
//        
//        Map<EnumMap<GHOST, MOVE>, Long> ghost_subtree = summed_visit_count.get(best_pacman_move);
//        EnumMap<GHOST, MOVE> best_ghost_move = null;
//      
//        assert(!ghost_subtree.isEmpty());
//        
//        for (EnumMap<GHOST, MOVE> ghost_move: ghost_subtree.keySet()) {
//            if (best_ghost_move==null||ghost_subtree.get(ghost_move)>ghost_subtree.get(best_ghost_move)) {
//                best_ghost_move = ghost_move;
//            }
//        }
//        
//        return best_ghost_move==null? MOVE.NEUTRAL: best_ghost_move.get(this.ghost);
    }

}
