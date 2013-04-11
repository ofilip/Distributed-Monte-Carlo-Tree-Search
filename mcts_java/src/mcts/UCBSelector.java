package mcts;

import java.util.EnumMap;
import java.util.List;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import utils.Pair;

public class UCBSelector implements Selector {
    private int trial_threshold = Constants.DEFAULT_UCB_SELECTION_TRIAL_THRESHOLD;
    private GuidedSimulator simulator;

    public UCBSelector(GuidedSimulator simulator) {
        this.simulator = simulator;
    }

    public UCBSelector(GuidedSimulator simulator, int trial_threshold) {
        this.simulator = simulator;
        this.trial_threshold = trial_threshold;
    }

    private Pair<MCNode,Action> best(MCNode node) {
        double best_val = Double.NEGATIVE_INFINITY;
        MCNode best = null;

        if (node.pacmanOnTurn()) {
            MOVE best_move = MOVE.NEUTRAL;
            for (MOVE move: node.pacman_children.keySet()) {
                MCNode child = node.pacman_children.get(move);
                double curr_val = child.ucbValue();

                if (curr_val>best_val) {
                    best_val = curr_val;
                    best = child;
                    best_move = move;
                }
            }
            return new Pair<MCNode,Action>(best, new PacmanAction(best_move));
        } else {
            EnumMap<GHOST,MOVE> best_move = Utils.NEUTRAL_GHOSTS_MOVES;
            for (EnumMap<GHOST,MOVE> move: node.ghosts_children.keySet()) {
                MCNode child = node.ghosts_children.get(move);
                double curr_val = child.ucbValue();

                if (curr_val>best_val) {
                    best_val = curr_val;
                    best = child;
                    best_move = move;
                }
            }
            return new Pair<MCNode,Action>(best, new GhostAction(best_move));
        }
    }

    @Override
    public Pair<MCNode,Action> select(MCNode node) {
        if (node.visitCount()<trial_threshold) {
            return simulator.nodeStep(node);
        }

        return best(node);
    }

//    @Override
//    public MCNode select(MCNode node, List<Action> action_list) {
//        MCNode result = select(node);
//
//        if (result.isPacmanNode()) {
//            PacmanNode pacman_result = (PacmanNode)result;
//            action_list.add(new PacmanAction(pacman_result.pacmanMove()));
//        } else {
//            GhostsNode ghost_result = (GhostsNode)result;
//            action_list.add(new GhostAction(ghost_result.ghostsMoves()));
//        }
//
//        return result;
//    }
}