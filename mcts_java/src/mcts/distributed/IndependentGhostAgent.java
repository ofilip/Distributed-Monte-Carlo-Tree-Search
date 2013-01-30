package mcts.distributed;

import communication.Channel;
import communication.MessageReceiver;
import communication.messages.MoveMessage;
import java.util.EnumMap;
import java.util.Map;
import mcts.AvgBackpropagator;
import mcts.GhostsTree;
import mcts.MySimulator;
import mcts.UCBSelector;
import mcts.Utils;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class IndependentGhostAgent extends FullMCTSGhostAgent { 
    public IndependentGhostAgent(GHOST ghost, int simulation_depth, double ucb_coef, boolean verbose) {
        super(ghost, simulation_depth, ucb_coef, verbose);
    }
    public IndependentGhostAgent(GHOST ghost, int simulation_depth, double ucb_coef) {
        this(ghost, simulation_depth, ucb_coef, false);
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
