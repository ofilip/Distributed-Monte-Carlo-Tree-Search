package mcts.distributed.agents;

import communication.MessageSender;
import communication.Priority;
import communication.messages.Message;
import communication.messages.RootMessage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.GhostsNode;
import mcts.MCNode;
import mcts.PacmanNode;
import mcts.Utils;
import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class RootExchangingAgent extends FullMCTSGhostAgent {
    private Map<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>> receivedRoots = new EnumMap<GHOST, EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>>(GHOST.class);
    private long totalSimulations = 0;
    private long totalReceivedRootsSize = 0;
    private final static GHOST VERBOSE_GHOST = GHOST.SUE;
    private int last_sent = 0;

    public RootExchangingAgent(final DistributedMCTSController controller, final GHOST ghost) {
        super(controller, ghost);
        hookMessageHandler(RootMessage.class, new MessageHandler() {
            @Override
            public void handleMessage(GhostAgent agent, Message message) {
                RootMessage roots_message = (RootMessage)message;
                //System.err.printf("[%s=>%s:%s] receiving %s\n", agent.ghost, ghost, controller.currentVirtualMillis(), message);
                receivedRoots.put(agent.ghost(), roots_message.getRoots());
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

    public boolean rootSendingActive() {
        return mctree.root().ghostsOnTurn()||mctree.root().halfstepFollows();
    }

    private EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> extractRoots() {
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);

        if (mctree.root().ghostsOnTurn()) {
            roots.put(MOVE.NEUTRAL, extractRoot(mctree.root()));
        } else if (mctree.root().halfstepFollows()){
            for (MCNode subtree: mctree.root().children()) {
                if (subtree.expanded()&&!subtree.game().gameOver()) {
                    PacmanNode pacman_node = (PacmanNode)subtree;
                    roots.put(pacman_node.pacmanMove(), extractRoot(subtree));
                }
            }
        } else return null;

        return roots;
    }

    private void sendMessages() {
        if (last_sent==0) {
            EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots = extractRoots();

            /* send messages only if next turn is ghosts turn */
            if (roots==null) return;

            if (verboseLevel.check(VerboseLevel.DEBUGGING)&&ghost==VERBOSE_GHOST) {
                System.out.printf("[%s:%s] Sending root: %s\n", ghost, controller.currentVirtualMillis(), roots);
                System.out.printf("%s", mctree.toString(mctree.root().halfstep()? 2: 1));
            }

            RootMessage message = new RootMessage(roots);
            //System.err.printf("[%s:%s] sending %s\n", ghost, controller.currentVirtualMillis(), message);
            for (MessageSender sender: messageSenders.values()) {
                sender.sendQueueFlushUnsent(RootMessage.class);
            }
            broadcastMessage(Priority.MEDIUM, message, true);
        }
        last_sent = (last_sent+1)%30; /* Way how to reduce expenses connected with root extraction */
    }

    @Override
    public void step() {
        receiveMessages();
        if (!Double.isNaN(mctree.iterate())) {
            totalSimulations++;
        }
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        last_sent = 0;
        totalReceivedRootsSize += currentReceivedRootsSize();

        if (!Utils.ghostsNeedAction(currentGame)) {
            lastFullMove = Utils.ghostsFollowRoads(currentGame);
            return lastFullMove.get(ghost);
        }

        /* Return move with best summed visit count */
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> summed_visit_count = new EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>>(MOVE.class);
        EnumMap<MOVE, Long> pacman_move_visit_count = new EnumMap<MOVE, Long>(MOVE.class);
        EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> my_roots = extractRoots();

        if (my_roots!=null) {
            receivedRoots.put(ghost, my_roots);
        }

        if (verboseLevel.check(VerboseLevel.VERBOSE)&&ghost==VERBOSE_GHOST) System.out.printf("[%s:%s] calculating move... my tree:\n%s\n", ghost, controller.currentVirtualMillis(), mctree.toString(2));
        for (EnumMap<MOVE, Map<EnumMap<GHOST, MOVE>, Long>> roots: receivedRoots.values()) {
            for (MOVE pacman_move: roots.keySet()) {
                Map<EnumMap<GHOST, MOVE>, Long> root = roots.get(pacman_move);
                Map<EnumMap<GHOST, MOVE>, Long> visit_count_map = summed_visit_count.get(pacman_move);
                if (verboseLevel.check(VerboseLevel.VERBOSE)&&ghost==VERBOSE_GHOST) {
                    System.out.printf("  Received: %s\n", root);
                }
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
        if (verboseLevel.check(VerboseLevel.VERBOSE)&&ghost==VERBOSE_GHOST) {
            System.out.printf("  Calculated map: %s\n", summed_visit_count);
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

        if (verboseLevel.check(VerboseLevel.VERBOSE)&&ghost==VERBOSE_GHOST) {
            System.out.printf("  Best pacman move: %s\n", best_pacman_move);
        }

        Map<EnumMap<GHOST, MOVE>, Long> ghost_subtree = summed_visit_count.get(best_pacman_move);
        EnumMap<GHOST, MOVE> best_ghost_move = null;

        assert(!ghost_subtree.isEmpty());

        for (EnumMap<GHOST, MOVE> ghost_move: ghost_subtree.keySet()) {
            if (best_ghost_move==null||ghost_subtree.get(ghost_move)>ghost_subtree.get(best_ghost_move)) {
                best_ghost_move = ghost_move;
            }
        }

        if (verboseLevel.check(VerboseLevel.VERBOSE)&&ghost==VERBOSE_GHOST) {
            System.out.printf("  Best ghost move: %s\n", best_ghost_move);
        }

        lastFullMove = best_ghost_move!=null? best_ghost_move: Utils.NEUTRAL_GHOSTS_MOVES;
        return best_ghost_move==null? MOVE.NEUTRAL: best_ghost_move.get(this.ghost);

    }

    public long currentReceivedRootsSize() {
        long sum = 0;
        for (GHOST ally: receivedRoots.keySet()) {
            if (ally==ghost) continue;
            EnumMap<MOVE,Map<EnumMap<GHOST, MOVE>, Long>> root = receivedRoots.get(ally);
            for (Map<EnumMap<GHOST, MOVE>, Long> ghostRoot: root.values()) {
                for (Long visitCount: ghostRoot.values()) {
                    sum += visitCount;
                }
            }
        }
        return sum;
    }

    public long totalReceivedRootsSize() { return totalReceivedRootsSize; }
    @Override public long totalSimulations() { return totalSimulations; }
    @Override public long calculatedSimulations() { return totalSimulations; }
}
