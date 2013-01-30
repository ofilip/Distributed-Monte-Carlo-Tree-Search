package mcts.distributed;

import communication.messages.Message;
import communication.MessageReceiver;
import communication.MessageSender;
import communication.messages.MoveMessage;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcts.AvgBackpropagator;
import mcts.GhostsTree;
import mcts.MySimulator;
import mcts.UCBSelector;
import mcts.Utils;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import utils.Pair;

public class MoveExchangingGhostAgent extends FullMCTSGhostAgent {   
    private Map<GHOST, MoveMessage> received_moves = new EnumMap<GHOST, MoveMessage>(GHOST.class);
    private int moves_message_interval;
    private long last_message_sending_time = 0;
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
    
    public MoveExchangingGhostAgent(final GHOST ghost, int simulation_depth, double ucb_coef, int moves_message_interval, boolean verbose) {
        super(ghost, simulation_depth, ucb_coef, verbose);
        hookMessageHandler(MoveMessage.class, new MessageHandler() {
            @Override
            public void handleMessage(GhostAgent agent, Message message) {
                MoveMessage moves_message = (MoveMessage)message;
                System.err.print(ghost+": "+"message received from "+agent.ghost+": "+message);
                received_moves.put(agent.ghost(), moves_message);
            }            
        });
        this.moves_message_interval = moves_message_interval;
    }
    
    public MoveExchangingGhostAgent(GHOST ghost, int simulation_depth, double ucb_coef, int moves_message_interval) {
        this(ghost, simulation_depth, ucb_coef, moves_message_interval, false);
    }
    
    private void sendMessages() {
        long current_time = System.currentTimeMillis();
        EnumMap<GHOST, MOVE> best_move = mctree.bestMove(current_game);
        MoveMessage message = new MoveMessage(best_move);
        
        if (current_time-last_message_sending_time>moves_message_interval) {
            for (MessageSender sender: message_senders.values()) {
                sender.send(message);
            }
            last_message_sending_time = current_time;
        }        
    }

    @Override
    public void step() {
        receiveMessages();
        mctree.iterate();
        sendMessages();
    }

    @Override
    public MOVE getMove() {
        /* Return strongest move (move supported by the most agents)
         * with priority defined by ordering on GHOST enum. */
        EnumMap<GHOST, MOVE> my_best_move = mctree.bestMove(current_game);
        received_moves.put(ghost, new MoveMessage(my_best_move)); /* add my current best move to received messages */
        
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
