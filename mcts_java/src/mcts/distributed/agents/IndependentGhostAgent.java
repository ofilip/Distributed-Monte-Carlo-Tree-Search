package mcts.distributed.agents;

import mcts.distributed.DistributedMCTSController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.VerboseLevel;

public class IndependentGhostAgent extends FullMCTSGhostAgent { 
    public IndependentGhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef, VerboseLevel verbose) {
        super(controller, ghost, simulation_depth, ucb_coef, verbose);
    }
    public IndependentGhostAgent(DistributedMCTSController controller, GHOST ghost, int simulation_depth, double ucb_coef) {
        this(controller, ghost, simulation_depth, ucb_coef, VerboseLevel.QUIET);
    }

    @Override
    public void step() {
        mctree.iterate();
    }

    @Override
    public MOVE getMove() {
        last_full_move = mctree.bestMove(current_game); /* in this context previous_game is current game */
        return last_full_move.get(ghost);
    }

}
