package mcts.distributed;

import communication.Message;
import communication.P2PChannel;
import communication.P2PNetwork;
import java.util.EnumMap;
import java.util.Map;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class DistributedMCTSController<G extends GhostAgent<M>, M extends Message> extends Controller<EnumMap<GHOST,MOVE>> {
    protected P2PNetwork<M> network;
    protected Map<GHOST, GhostAgent<M>> agents = new EnumMap<GHOST, GhostAgent<M>>(GHOST.class);
    protected int current_ghost = 0;
    protected EnumMap<GHOST,MOVE> moves = new EnumMap<GHOST,MOVE>(GHOST.class);
    protected boolean verbose;
    
    private static GHOST ghosts[] = {GHOST.BLINKY, GHOST.PINKY, GHOST.INKY, GHOST.SUE};
    protected static final long MILLIS_TO_FINISH = 20; 
    
    public DistributedMCTSController(long channel_transmission_speed, boolean verbose) {        
        this.network = new P2PNetwork<M>(channel_transmission_speed);
        this.verbose = verbose;
    }
    
    public DistributedMCTSController addGhostAgent(GhostAgent ghost_agent) {
        assert !agents.containsKey(ghost_agent.ghost());
        for (GhostAgent ally: agents.values()) {
            P2PChannel out_channel = network.openChannel(String.format("%s$%s", ghost_agent.ghostName(), ally.ghostName()));
            P2PChannel in_channel = network.openChannel(String.format("%s$%s", ally.ghostName(), ghost_agent.ghostName()));
            ghost_agent.addAlly(out_channel, ally);
            ally.addAlly(in_channel, ghost_agent);
        }
        agents.put(ghost_agent.ghost(), ghost_agent);
        return this;
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        long start_time = System.currentTimeMillis();
        assert agents.size()==4;
        
        /* update agents' trees */
        for (GHOST ghost: GHOST.values()) {
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
            double computation_time = (System.currentTimeMillis()-start_time)/1000.0;
            System.out.printf("MOVE INFO [node_index=%d]: computation time: %.3f s, move: %s\n", 
                    game.getPacmanCurrentNodeIndex(), computation_time, moves);    
            if (timeDue - System.currentTimeMillis()<0) {
            System.err.printf("Missed turn, delay: %d ms\n", -(timeDue - System.currentTimeMillis()));
        }    
        }
        return moves;
    }
}
