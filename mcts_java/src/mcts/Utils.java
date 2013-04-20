package mcts;

import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/**
 * Miscellaneous functions.
 */
public class Utils {
//    static Random random = new Random(10);
    public final static EnumMap<GHOST, MOVE> NEUTRAL_GHOSTS_MOVES = new EnumMap<GHOST, MOVE>(GHOST.class);

    static {
        for (GHOST ghost: GHOST.values()) {
            NEUTRAL_GHOSTS_MOVES.put(ghost, MOVE.NEUTRAL);
        }
    }

    private Utils() {}

//    public static Random rnd() {
//        return random;
//    }

    public static double UCB1(double v_i, double n_p, double n_i, double coef) {
        return n_i==0? Double.POSITIVE_INFINITY: (v_i + coef*Math.sqrt(Math.log(n_p)/(n_i)));
    }

    public static boolean moveInArray(MOVE move, MOVE[] arr) {
        for (MOVE m: arr) {
            if (m==move) {
                return true;
            }
        }

        return false;
    }

//    public static MOVE reverseLastMove(Game game, int pos, MOVE last_move) {
//        return reverse(game, pos, last_move.opposite());
//    }

    public static MOVE reverse(Game game, int pos, MOVE move) {
        //assert move!=MOVE.NEUTRAL;
        MOVE[] possible_moves = game.getPossibleMoves(pos);
        if (possible_moves.length>2) {
            assert moveInArray(move.opposite(), possible_moves);
            return move.opposite();
        } else if (possible_moves.length==0) {
            assert move==MOVE.NEUTRAL;
            return MOVE.NEUTRAL;
        } else {
            assert possible_moves.length==2;
            if (possible_moves[0]==move) {
                return possible_moves[1];
            } else {
                assert possible_moves[1]==move;
                return possible_moves[0];
            }
        }
    }

    public static boolean globalReversalHappened(Game game) {
        int last_reversal = game.getTimeOfLastGlobalReversal();
        int total_time = game.getTotalTime();
        return last_reversal+1==total_time&&!game.wasPowerPillEaten();

    }

    public static MOVE[] getGhostsPossibleMoves(Game game, GHOST ghost) {
        int pos = game.getGhostCurrentNodeIndex(ghost);
        MOVE last_move = game.getGhostLastMoveMade(ghost);

        if (game.wasPowerPillEaten()) {
            if (last_move==MOVE.NEUTRAL) {
                return new MOVE[]{MOVE.NEUTRAL};
            } else {
                return new MOVE[]{last_move.opposite()};
            }
        } else {
            MOVE[] possible_moves = game.getPossibleMoves(pos, last_move);
            return possible_moves.length>0? possible_moves: new MOVE[]{MOVE.NEUTRAL};
        }
    }

    public static void getGhostsPossibleMoves(EnumMap<GHOST, MOVE[]> ghosts_possible_moves, Game game) {
        for (GHOST ghost: GHOST.values()) {
            ghosts_possible_moves.put(ghost, getGhostsPossibleMoves(game, ghost));
        }
    }

    public static EnumMap<GHOST, MOVE[]> getGhostsPossibleMoves(Game game) {
        EnumMap<GHOST, MOVE[]> ghosts_possible_moves = new EnumMap<GHOST, MOVE[]>(GHOST.class);
        getGhostsPossibleMoves(ghosts_possible_moves, game);
        return ghosts_possible_moves;
    }

    public static MOVE randomGhostsMove(Game game, GHOST ghost, Random random) {
        MOVE[] possible_moves = getGhostsPossibleMoves(game, ghost);
        int move_index = random.nextInt(possible_moves.length);
        return possible_moves[move_index];
    }

    public static EnumMap<GHOST, MOVE> randomGhostsMoves(Game game, Random random) {
        EnumMap<GHOST, MOVE> res = new EnumMap<GHOST, MOVE>(GHOST.class);

        for (GHOST ghost: GHOST.values()) {
            res.put(ghost, randomGhostsMove(game, ghost, random));
        }

        return res;
    }

    public static MOVE[] getAllPacmansPossibleMoves(Game game) {
        return getPacmansPossibleMoves(game, PACMAN_REVERSAL.ALWAYS);
    }

    public enum PACMAN_REVERSAL {
        ALWAYS,
        XROADS_ONLY,
        NEVER
    }

    public static MOVE[] getPacmansPossibleMoves(Game game, PACMAN_REVERSAL reversal) {
        int pos = game.getPacmanCurrentNodeIndex();

        if (reversal==PACMAN_REVERSAL.ALWAYS||(reversal==PACMAN_REVERSAL.XROADS_ONLY&&game.isJunction(pos))) {
            return game.getPossibleMoves(pos);
        } else {
            MOVE last_move = game.getPacmanLastMoveMade();
            MOVE[] possible_moves = game.getPossibleMoves(pos, last_move);
            return possible_moves;
        }

    }

    public static boolean wasAnyGhostEaten(Game game) {
        for (GHOST ghost: GHOST.values()) {
            if (game.wasGhostEaten(ghost)) {
                return true;
            }
        }

        return false;
    }

    public static boolean pacmanOnCrossroad(Game game) {
        MOVE[] pacman_moves = Utils.getAllPacmansPossibleMoves(game);
        return pacman_moves.length>2;
    }

    public static boolean ghostOnCrossroad(Game game, GHOST ghost) {
        MOVE[] ghosts_moves = Utils.getGhostsPossibleMoves(game, ghost);
        return ghosts_moves.length>2;
    }

    public static boolean ghostOnCrossroad(Game game) {
        for (GHOST ghost: GHOST.values()) {
            if (ghostOnCrossroad(game, ghost)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param game
     * @return Move following current pacman's path.
     * @note Expects pacmanOnCrossroad(game)==false (otherwise behaviour is undefined)
     */
    public static MOVE pacmanFollowRoad(Game game) {
        return getPacmansPossibleMoves(game, PACMAN_REVERSAL.NEVER)[0];
    }

    public static EnumMap<GHOST, MOVE> ghostsFollowRoads(Game game) {
        EnumMap<GHOST, MOVE> ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        for (GHOST ghost: GHOST.values()) {
            MOVE[] ghost_moves = getGhostsPossibleMoves(game, ghost);
            assert ghost_moves.length==1;
            ghosts_moves.put(ghost, ghost_moves[0]);
        }
        return ghosts_moves;
    }

    public static void followRoads(Game game) {
        game.advanceGameWithPowerPillReverseOnly(pacmanFollowRoad(game), ghostsFollowRoads(game).clone());
    }

    public static MOVE randomPacmanMove(Game game, Random random) {
        return randomPacmanMove(game, PACMAN_REVERSAL.ALWAYS, random);
    }

    public static MOVE randomPacmanMove(Game game, PACMAN_REVERSAL reversal, Random random) {
        MOVE[] possible_moves = getPacmansPossibleMoves(game, reversal);
        int move_index = random.nextInt(possible_moves.length);

        return possible_moves[move_index];
    }

    public static MOVE randomPacmanMoveReversalPenalized(Game game, double reversal_probability_coef, Random random) {
        MOVE[] possible_moves = getAllPacmansPossibleMoves(game);
        MOVE last_move = game.getPacmanLastMoveMade();
        double sum = possible_moves.length-(1-reversal_probability_coef);
        double rnd = sum*random.nextDouble();
        double acc = 1;
        int move_index = 0;
        while (acc<rnd) {
            acc += possible_moves[move_index]==last_move? reversal_probability_coef: 1;
            move_index++;
        }

        return possible_moves[move_index];
    }

    public static boolean implies(boolean A, boolean B) {
        return !A||B;
    }

    public static boolean compareGames(Game g1, Game g2) {
        return g1.getGameState().equals(g2.getGameState());
    }

    public static boolean ghostMovesEqual(EnumMap<GHOST, MOVE> m1, EnumMap<GHOST, MOVE> m2) {
        for (GHOST ghost: GHOST.values()) {
            if (m1.get(ghost)!=m2.get(ghost)) {
                return false;
            }
        }
        return true;
    }

    public static double addToAvg(double curr_avg, int curr_count, double new_value) {
        return (curr_avg*curr_count + new_value)/(1+curr_count);
    }

    public static EnumMap<GHOST, MOVE> lastGhostsMoves(Game game) {
        EnumMap<GHOST, MOVE> last_ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        for (GHOST ghost: GHOST.values()) {
            last_ghosts_moves.put(ghost, game.getGhostLastMoveMade(ghost));
        }
        return last_ghosts_moves;
    }

    public static EnumMap<GHOST, MOVE> lastGhostsDecisionMoves(Game game, Game previous_game) {
        EnumMap<GHOST, MOVE> decision_moves = lastGhostsMoves(game);
        decisionMoves(decision_moves, previous_game);
        return decision_moves;
    }

    /**
     * @param ghosts_moves Ghosts moves to be reduced to decision (value is changed!).
     * @param game Current game.
     * Assigns MOVE.NEUTRAL to move of ghosts which doesn't require action
     */
    public static void decisionMoves(EnumMap<GHOST, MOVE> ghosts_moves, Game game) {
        for (GHOST ghost: GHOST.values()) {
            if (!ghostNeedAction(game, ghost)) {
                ghosts_moves.put(ghost, MOVE.NEUTRAL);
            }
        }
    }

    public static EnumMap<GHOST, MOVE> createDecisionMoves(EnumMap<GHOST, MOVE> ghosts_moves, Game game) {
        EnumMap<GHOST, MOVE> decision_moves = ghosts_moves.clone();
        decisionMoves(decision_moves, game);
        return decision_moves;
    }

    /**
     * @param game
     * @return Null on success or root's advanced game on failure.
     */
    public static Game testRoot(Game game, MCTree mcTree) {
        Game tree_game = mcTree.root().game();
        Game advanced = Decision.nextDecision(game, mcTree.root().pacmanDecisionGap()).game;
        if (Utils.compareGames(advanced, tree_game)) {
            return null;
        } else {
            return advanced;
        }
    }

    public static boolean ghostNeedAction(Game game, GHOST ghost) {
        return game.doesGhostRequireAction(ghost)&&getGhostsPossibleMoves(game, ghost).length>=2;
    }

    public static boolean ghostsNeedAction(Game game) {
        for (GHOST ghost: GHOST.values()) {
            if (ghostNeedAction(game, ghost)) {
                return true;
            }
        }

        return false;
    }

    public static boolean ghostMovesEquals(EnumMap<GHOST, MOVE> moves1, EnumMap<GHOST, MOVE> moves2) {
        for (GHOST ghost: GHOST.values()) {
            if (moves1.get(ghost)!=moves2.get(ghost)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param bits Number of bits.
     * @return Number of bytes (as a ceiling) necessary to transmit bits.
     */
    public static long bitsToBytes(long bits) {
        /*  0<=bits<=7 => (bits+1)/8 = bits/8
         *  bits==8 => (bits+1)/8 = 1 + bits/8
         */
        return (bits+1)/8;
    }
}

