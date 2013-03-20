package mcts.distributed;

import utils.VirtualTimer;
import mcts.distributed.agents.GhostAgent;
import communication.messages.Message;
import communication.Channel;
import communication.Network;
import java.util.EnumMap;
import java.util.Map;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class DistributedMCTSController<G extends GhostAgent> extends Controller<EnumMap<GHOST,MOVE>> implements VirtualTimer {
    protected Network network;
    protected Map<GHOST, GhostAgent> agents = new EnumMap<GHOST, GhostAgent>(GHOST.class);
    protected int current_ghost = 0;
    protected EnumMap<GHOST,MOVE> moves = new EnumMap<GHOST,MOVE>(GHOST.class);
    protected boolean verbose;
    private long channel_buffer_size;
    
    private static GHOST ghosts[] = {GHOST.BLINKY, GHOST.PINKY, GHOST.INKY, GHOST.SUE};
    protected static final long MILLIS_TO_FINISH = 20;
    private long move_number = 0;
    
    /**
     * 
     * @param channel_transmission_speed Transmission speed in bytes per second.
     * @param verbose Verbose level.
     */
    public DistributedMCTSController(long channel_transmission_speed, long channel_buffer_size, boolean verbose) {        
        this.network = new Network(channel_transmission_speed);
        this.network.setTimer(this);
        this.verbose = verbose;
        this.channel_buffer_size = channel_buffer_size;
    }
    
    public DistributedMCTSController addGhostAgent(GhostAgent ghost_agent) {
        assert !agents.containsKey(ghost_agent.ghost());
        for (GhostAgent ally: agents.values()) {
            Channel out_channel = network.openChannel(String.format("%s$%s", ghost_agent.ghostName(), ally.ghostName()), channel_buffer_size);
            Channel in_channel = network.openChannel(String.format("%s$%s", ally.ghostName(), ghost_agent.ghostName()), channel_buffer_size);
            ghost_agent.addAlly(out_channel, ally);
            ally.addAlly(in_channel, ghost_agent);
        }
        agents.put(ghost_agent.ghost(), ghost_agent);
        return this;
    }
    
    @Override public long currentMillis() { return System.currentTimeMillis()/agents.size(); }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        long start_time = System.currentTimeMillis();
        assert agents.size()==4;
        move_number++;
        
        /* update agents' trees */
        for (GHOST ghost: GHOST.values()) {
            agents.get(ghost).truncateNetworkBuffers();
            agents.get(ghost).updateTree(game);
        }
        
        /* alternately run logic of agents (simulates parallel run) */
        do {
            agents.get(ghosts[current_ghost]).step();
            current_ghost = (current_ghost+1)%4;
        } while ((System.currentTimeMillis()+MILLIS_TO_FINISH)<timeDue);
        
        /* gather ghosts' moves and return result */
        for (GHOST ghost: GHOST.values()) {
            moves.put(ghost, agents.get(ghost).getMove());
        }        
        if (verbose) {
            int total_simulations_count = 0;

            for (GhostAgent agent: agents.values()) {
                total_simulations_count += agent.getTree().size();
            }
            double computation_time = (System.currentTimeMillis()-start_time)/1000.0;
            System.out.printf("MOVE INFO [node_index=%d]: computation time: %.3f s, simulations: %s, move (no.%s): %s\n", 
                    game.getPacmanCurrentNodeIndex(), computation_time, total_simulations_count, move_number, moves);    
            if (timeDue - System.currentTimeMillis()<0) {
                System.err.printf("Missed turn, delay: %d ms\n", -(timeDue - System.currentTimeMillis()));
            }    
        }
        return moves;
    }
}
