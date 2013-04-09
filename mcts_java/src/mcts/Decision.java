package mcts;

import java.util.EnumMap;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class Decision {
    public static final int PACMAN_DECISION_GAP = 8;

    public enum DecisionCause {
        CROSSROAD_REACHED("xrd"),
        MAX_GAP("gap"),
        POWERPILL_EATEN("pll"),
        GHOST_EATEN("gst"),
        STEP_BEFORE_POWERPILL("bpl"),
        NEW_LIFE("new"),
        NONE("non"); /* no decision */

        String short_name;

        DecisionCause(String short_name) {
            this.short_name = short_name;
        }

        public String shortName() {
            return short_name;
        }
    }

    public Decision(int ticks, Game game, MOVE[] pacman_possible_moves, EnumMap<GHOST, MOVE[]> ghosts_possible_moves,
            DecisionNeededFrom decision_needed_from, int pacman_decision_gap, DecisionCause pacman_decision_cause) {
        this.ticks = ticks;
        this.game = game;
        this.pacman_possible_moves = pacman_possible_moves;
        this.ghosts_possible_moves = ghosts_possible_moves;
        this.decision_needed_from = decision_needed_from;
        this.pacman_decision_gap = pacman_decision_gap;
        this.pacman_decision_cause = pacman_decision_cause;
    }

    public DecisionNeededFrom decision_needed_from;
    public int ticks; /* number of ticks to decision */
    public Game game;
    public MOVE[] pacman_possible_moves;
    public EnumMap<GHOST, MOVE[]> ghosts_possible_moves;
    public int pacman_decision_gap;
    public DecisionCause pacman_decision_cause;

    public boolean pacmansDecision() {
        return decision_needed_from==DecisionNeededFrom.PACMAN_ONLY
                ||decision_needed_from==DecisionNeededFrom.BOTH;
    }

    public boolean ghostsDecision() {
        return decision_needed_from==DecisionNeededFrom.GHOSTS_ONLY
                ||decision_needed_from==DecisionNeededFrom.BOTH;
    }

    public boolean jointDecision() {
        return decision_needed_from==DecisionNeededFrom.BOTH;
    }

    private static class DecisionStepData {
        DecisionStepData(Game game, int pacman_decision_gap) {
            this.game = game;
            this.pacman_decision_gap = pacman_decision_gap;
        }

        Game game;
        EnumMap<GHOST, MOVE> ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        MOVE[] pacman_possible_moves = null;
        EnumMap<GHOST, MOVE[]> ghosts_possible_moves = new EnumMap<GHOST, MOVE[]>(GHOST.class);
        int pacman_decision_gap = 0;
        DecisionNeededFrom decision_needed_from;
        DecisionCause pacman_decision_cause = DecisionCause.NONE;
        int ticks = 0;
    }

    /* Pacman decides if:
     * - crossroad reached
     * - powerpill eaten
     * - next step on road leads to powerpill
     * - each PACMAN_DECISION_GAP ticks at most
     */
    private static boolean isPacmansDecision(DecisionStepData decision_data) {
        Game game = decision_data.game;

        /* crossroad reached? */
        if (decision_data.pacman_possible_moves.length>2) {
            decision_data.pacman_decision_gap = 0;
            decision_data.pacman_decision_cause = DecisionCause.CROSSROAD_REACHED;
            return true;
        }

        /* no decision last PACMAN_DECISION_GAP steps */
        if (decision_data.pacman_decision_gap==PACMAN_DECISION_GAP) {
            decision_data.pacman_decision_gap = PACMAN_DECISION_GAP;
            decision_data.pacman_decision_cause = DecisionCause.MAX_GAP;
            return true;
        }

        /* power pill eaten */
        if (game.wasPowerPillEaten()) {
            decision_data.pacman_decision_gap = 0;
            decision_data.pacman_decision_cause = DecisionCause.POWERPILL_EATEN;
            return true;
        }

        if (game.wasPacManEaten()) {
            /* pacman goes left always in new level */
            decision_data.pacman_decision_gap = 0;
            decision_data.pacman_decision_cause = DecisionCause.NEW_LIFE;
            return true;
        }

        /* ghost eaten */
        if (Utils.wasAnyGhostEaten(game)) {
            decision_data.pacman_decision_gap = 0;
            decision_data.pacman_decision_cause = DecisionCause.GHOST_EATEN;
            return true;
        }

        int pacman_node = game.getPacmanCurrentNodeIndex();
        MOVE pacman_move = decision_data.pacman_possible_moves[0];
        int next_node = game.getNeighbour(pacman_node, pacman_move);
        int power_pill_index = game.getPowerPillIndex(next_node);

        /* step before power pill */
        if (power_pill_index!=-1&&game.isPowerPillStillAvailable(power_pill_index)) {
            decision_data.pacman_decision_gap = 0;
            decision_data.pacman_decision_cause = DecisionCause.STEP_BEFORE_POWERPILL;
            return true;
        }

        /* no pacman decision branching */
        decision_data.pacman_decision_gap = decision_data.pacman_decision_gap%PACMAN_DECISION_GAP;
        decision_data.pacman_decision_gap++;
            decision_data.pacman_decision_cause = DecisionCause.NONE;
            return false;
    }

    private static boolean isGhostsDecision(DecisionStepData decision_data) {
        for (GHOST ghost: GHOST.values()) {
            if (decision_data.ghosts_possible_moves.get(ghost).length>1&&decision_data.game.doesGhostRequireAction(ghost)) {
                return true;
            }
        }

        return false;
    }

    private static DecisionNeededFrom decisionStep(DecisionStepData decision_data) {
        Game game = decision_data.game;
        decision_data.pacman_possible_moves = Utils.getPacmansPossibleMoves(decision_data.game, Utils.PACMAN_REVERSAL.ALWAYS);
        Utils.getGhostsPossibleMoves(decision_data.ghosts_possible_moves, decision_data.game);

        boolean is_pacman_decision = isPacmansDecision(decision_data);
        boolean is_ghost_decision = isGhostsDecision(decision_data);
        decision_data.decision_needed_from = DecisionNeededFrom.get(is_pacman_decision, is_ghost_decision);

        if (decision_data.decision_needed_from==DecisionNeededFrom.NOBODY) {
            for (GHOST ghost: GHOST.values()) {
                MOVE ghost_move = decision_data.ghosts_possible_moves.get(ghost)[0];
                decision_data.ghosts_moves.put(ghost, ghost_move);
            }

            MOVE pacman_move = Utils.getPacmansPossibleMoves(decision_data.game, Utils.PACMAN_REVERSAL.NEVER)[0];
            game.advanceGameWithPowerPillReverseOnly(pacman_move, decision_data.ghosts_moves/*.clone() not necessary*/);
            decision_data.ticks++;
        }

        return decision_data.decision_needed_from;
    }

    public static Decision nextDecision(Game game, int pacman_decision_gap) {
        return nextDecision(game, pacman_decision_gap, false);
    }


    public static Decision nextDecision(Game game, int pacman_decision_gap, boolean game_copied) {
        game = game_copied? game: game.copy();
        DecisionStepData decision_data = new DecisionStepData(game, pacman_decision_gap);

        while (decisionStep(decision_data)==DecisionNeededFrom.NOBODY) {/*login is in decisionStep()*/}

        return new Decision(decision_data.ticks, decision_data.game, decision_data.pacman_possible_moves, decision_data.ghosts_possible_moves, decision_data.decision_needed_from, decision_data.pacman_decision_gap, decision_data.pacman_decision_cause);
    }

    public static Decision nextDecisionAfterMove(Game game, int pacman_decison_gap, MOVE pacman_move, EnumMap<GHOST, MOVE> ghosts_moves) {
        Game advanced = game.copy();
        advanced.advanceGameWithPowerPillReverseOnly(pacman_move, ghosts_moves.clone());
        Decision decision = nextDecision(advanced, pacman_decison_gap, true);
        decision.ticks++;
        return decision;
    }
}
