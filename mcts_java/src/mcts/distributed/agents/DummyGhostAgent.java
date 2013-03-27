package mcts.distributed.agents;

import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class DummyGhostAgent extends FullMCTSGhostAgent { 
    public long total_simulations = 0;
    
    public DummyGhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
    }
    public DummyGhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef) {
        this(controller, ghost, simulation_depth, ucb_coef, VerboseLevel.QUIET);
    }

    @Override
    public void step() {
        if (!Double.isNaN(mctree.iterate())) {
            total_simulations++;
        }
    }

    @Override
    public MOVE getMove() {
        last_full_move = mctree.bestMove(current_game); /* in this context previous_game is current game */
        return last_full_move.get(ghost);
    }

    @Override
    public long totalSimulations() {
        return total_simulations;
    }
}
