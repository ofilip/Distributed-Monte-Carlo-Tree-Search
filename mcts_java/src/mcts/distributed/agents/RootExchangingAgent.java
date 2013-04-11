package mcts.distributed.agents;

import communication.Priority;
import communication.messages.Message;
import communication.messages.RootMessage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.GhostsNode;
import mcts.MCNode;
import mcts.PacmanNode;
import mcts.distributed.DistributedMCTSController;
import mcts.distributed.IntervalHistory;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class RootExchangingAgent extends FullMCTSGhostAgent {
    private Map<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>> received_roots = new EnumMap<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>>(GHOST.class);
    private IntervalHistory interval_history = new IntervalHistory(5);
    private long last_message_sending_time = 0;
    private long total_simulations;

    public RootExchangingAgent(DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);
        hookMessageHandler(RootMessage.class, new MessageHandler() {
            @Override
            public void handleMessage(GhostAgent agent, Message message) {
                RootMessage roots_message = (RootMessage)message;
                received_roots.put(agent.ghost(), roots_message.getRoots());
            }
        });
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
        } else if (mctree.root().halfstep()){
            for (MCNode subtree: mctree.root().children()) {
                if (subtree.pacmanOnTurn()) {
                    return null;
                }
                PacmanNode pacman_node = (PacmanNode)subtree;
                roots.put(pacman_node.pacmanMove(), extractRoot(subtree));
            }
        } else return null;

        return roots;
    }

    private void sendMessages() {
        long current_time = controller.currentMillis();
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots = extractRoots();

        /* send messages only if next turn is ghosts turn */
        if (roots==null) return;

        RootMessage message = new RootMessage(roots);
        broadcastMessageIfLowBuffer(Priority.MEDIUM, message, (long)Math.floor(0.8*interval_history.averageInterval()));
        if (current_time!=0) interval_history.putTime(current_time-last_message_sending_time);
        last_message_sending_time = current_time;
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            total_simulations++;
        }
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        /* Return move with best summed visit count */
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> summed_visit_count = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);
        EnumMap<MOVE, Long> pacman_move_visit_count = new EnumMap<MOVE, Long>(MOVE.class);
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> my_roots = extractRoots();

        if (my_roots!=null) {
            received_roots.put(ghost, my_roots);
        }

        for (EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots: received_roots.values()) {
            for (MOVE pacman_move: roots.keySet()) {
                Map<EnumMap<GHOST, MOVE>, Long> root = roots.get(pacman_move);
                Map<EnumMap<GHOST, MOVE>, Long> visit_count_map = summed_visit_count.get(pacman_move);

                if (visit_count_map==null) {
                    visit_count_map = new HashMap<EnumMap<GHOST, MOVE>, Long>();
                    summed_visit_count.put(pacman_move, visit_count_map);
                    pacman_move_visit_count.put(pacman_move, new Long(0));
                }

                for (EnumMap<GHOST, MOVE> ghost_move: root.keySet()) {
                    Long sum = visit_count_map.get(ghost_move);
                    Long pacman_sum = pacman_move_visit_count.get(pacman_move);
                    Long count = root.get(ghost_move);

                    if (pacman_sum==null) pacman_sum = new Long(0);
                    if (sum==null) sum = new Long(0);
                    visit_count_map.put(ghost_move, sum+count);
                    pacman_move_visit_count.put(pacman_move, pacman_sum+count);
                }
            }
        }

        MOVE best_pacman_move = null;

        if (pacman_move_visit_count.isEmpty()) {
            return MOVE.NEUTRAL;
        }

        for (MOVE pacman_move: pacman_move_visit_count.keySet()) {
            if (best_pacman_move==null||pacman_move_visit_count.get(pacman_move)>pacman_move_visit_count.get(best_pacman_move)) {
                best_pacman_move = pacman_move;
            }
        }

        Map<EnumMap<GHOST, MOVE>, Long> ghost_subtree = summed_visit_count.get(best_pacman_move);
        EnumMap<GHOST, MOVE> best_ghost_move = null;

        assert(!ghost_subtree.isEmpty());

        for (EnumMap<GHOST, MOVE> ghost_move: ghost_subtree.keySet()) {
            if (best_ghost_move==null||ghost_subtree.get(ghost_move)>ghost_subtree.get(best_ghost_move)) {
                best_ghost_move = ghost_move;
            }
        }

        return best_ghost_move==null? MOVE.NEUTRAL: best_ghost_move.get(this.ghost);
    }

    @Override
    public long totalSimulations() {
        return total_simulations;
    }

}
