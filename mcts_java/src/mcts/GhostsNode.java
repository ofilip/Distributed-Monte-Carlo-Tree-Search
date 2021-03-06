package mcts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.Decision;
import mcts.Decision.DecisionCause;
import mcts.Moves;
import mcts.Utils;
import mcts.Utils.PACMAN_REVERSAL;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostsNode extends MCNode {
    EnumMap<GHOST, MOVE> ghosts_moves;

//    @Override
//    protected MCNode copy(MCTree tree, MCNode parent, long depth) {
//        return new GhostsNode(tree, this, parent, depth);
//    }
//
//    protected GhostsNode(MCTree tree, GhostsNode node, MCNode parent, long depth) {
//        super(tree, node, parent, depth);
//        this.ghosts_moves = node.ghosts_moves.clone();
//    }

    protected GhostsNode(MCTree tree, MCNode parent, Game game, EnumMap<GHOST, MOVE> ghosts_moves,
                         int initial_ticks, int pacman_decision_gap, long totalTicks) {

        super(tree, parent, game, initial_ticks, pacman_decision_gap, totalTicks);
        this.ghosts_moves = ghosts_moves==null? null: ghosts_moves.clone();
    }

    protected GhostsNode(MCTree tree, MCNode parent, Game parent_game, EnumMap<GHOST, MOVE> ghosts_moves, MOVE[] pacmans_possible_moves,
                         int pacman_decision_gap, DecisionCause pacman_decision_cause, long totalTicks) {
        this(tree, parent, parent_game.copy(), ghosts_moves, 0, pacman_decision_gap, totalTicks);
        pacman_children = new EnumMap<MOVE, PacmanNode>(MOVE.class);
        halfstep = true;
        this.decision_cause = pacman_decision_cause;
        for (int i=0; i<pacmans_possible_moves.length; i++) {
            MOVE pacman_move = pacmans_possible_moves[i];
            Game child_game = parent_game.copy();
            child_game.advanceGameWithPowerPillReverseOnly(pacman_move, ghosts_moves.clone());
            Decision decision = Decision.nextDecision(child_game, pacman_decision_gap);
            decision.ticks++;
            pacman_children.put(pacman_move, PacmanNode.createUnvisitedNode(tree, this, pacman_move, decision.game, decision.ticks,
                                                                            decision.pacman_decision_gap, totalTicks+decision.ticks));
        }
    }

    static GhostsNode createUnvisitedNode(MCTree tree, MCNode parent, EnumMap<GHOST, MOVE> ghosts_moves, int pacman_decision_gap, long totalTicks) {
        return createUnvisitedNode(tree, parent, ghosts_moves, null, 0, pacman_decision_gap, totalTicks);
    }

    static GhostsNode createUnvisitedNode(MCTree tree, MCNode parent, EnumMap<GHOST, MOVE> ghosts_moves, Game game, int initial_ticks, int pacman_decision_gap, long totalTicks) {
        return new GhostsNode(tree, parent, game, ghosts_moves, initial_ticks, pacman_decision_gap, totalTicks);
    }

    /**
     * Joint node means that the node has children with moves played simultaneously in game (but move in the node
     * is played first (before children turns) for selection purposes. The node is immediately expanded
     * @param pacmans_moves Moves for node's children.
     */
    static GhostsNode createJointNode(MCTree tree, MCNode parent, EnumMap<GHOST, MOVE> ghosts_moves, MOVE[] pacmans_possible_moves, int pacman_decision_gap, DecisionCause pacman_decision_cause, long totalTicks) {
        GhostsNode node = new GhostsNode(tree, parent, parent.game, ghosts_moves, pacmans_possible_moves, pacman_decision_gap, pacman_decision_cause, totalTicks);
        return node;
    }

    static GhostsNode createRoot(MCTree tree, Game game) {
        Decision decision = Decision.nextDecision(game, 0);
        GhostsNode root = new GhostsNode(tree, null, decision.game.copy(), null, decision.ticks, decision.pacman_decision_gap, 0);
        root.visit_count = 1;
        root.calculated_visit_count = 1;
        root.expand();
        return root;
    }

    @Override
    protected StringBuilder movesToString(StringBuilder result) {
        if (ghosts_moves!=null) {
            for (Map.Entry<GHOST, MOVE> e: this.ghosts_moves.entrySet()) {
                if (e.getValue()!=MOVE.NEUTRAL) {
                    result.append(" ").append(e.getKey()).append("=").append(e.getValue());
                }
            }
        }
        return result;
    }

    @Override
    protected StringBuilder typeToString(StringBuilder result) {
        result.append("GHOSTS");
        return result;
    }

    public EnumMap<GHOST, MOVE> ghostsMoves() {
        return ghosts_moves;
    }

    @Override
    public boolean isPacmanNode() {
        return false;
    }

    @Override
    protected void advanceGame(Game game) {
        MOVE pacman_move = Utils.pacmanFollowRoad(game);
        game.advanceGameWithPowerPillReverseOnly(pacman_move, ghosts_moves.clone());
    }

    @Override
    public double ucbValue() {
        assert !isRoot();

        /* Consider negative value for opponents turn */
        return Utils.UCB1(-this.value(), parent.visit_count, this.visit_count, tree.ucb1_coef);
    }
}
