package mcts;

import java.util.EnumMap;
import java.util.Random;
import mcts.Moves;
import mcts.Utils;
import mcts.Utils.PACMAN_REVERSAL;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;
import utils.Pair;

/**
 * Simulator guided by simplified starter controllers.
 * Simulation based on ideas (simplified) described in
 * 'Nozomu Ikehata and Takeshi Ito: Monte-Carlo Tree Search in Ms. Pac-Man',
 * StarterPacMan and the Legacy ghosts.
 */
public class GuidedSimulator implements Simulator {
    public final static double DEFAULT_RANDOM_MOVE_PROB = 0.5;
    public final static double DEFAULT_DEATH_WEIGHT = 0.05;
    public final static int MIN_DISTANCE = 20;
    public final static int PILL_PROXIMITY = 15;

    public final static int[] MAX_SCORES = new int[4];
    public final static int[] MAX_PILLS = new int[4];

    static {
        for (int i=0; i<4; i++) {
            Game game = new Game(0, i);
            MAX_SCORES[i] = 12000 + 200 + 10*game.getPillIndices().length;
        }
    }

    private double random_move_prob = 0.5;
    private int max_depth;
    private Random random;
    private double death_weight;

    public static double sigm(double x) {
        return 1/(1+Math.exp(-x));
    }

//    public GuidedSimulator(int max_depth, long seed) {
//        this(max_depth, seed, DEFAULT_RANDOM_MOVE_PROB, DEFAULT_DEATH_WEIGHT);
//    }

    public GuidedSimulator(int max_depth, long seed, double random_move_probability, double death_weight) {
        assert(random_move_probability>=0&&random_move_probability<=1);
        this.random = new Random(seed);
        this.max_depth = max_depth;
        this.random_move_prob = random_move_probability;
        this.death_weight = death_weight;
    }

    private MOVE choosePacmanMove(Game game) {
        /* Simplified strategy of StarterPacMan:
         * 1. if a ghost is too close, then pacman tries to flee
         * 2. if a blue ghost is nearby, pacman hunts it
         * 3. otherwise pacman choose random way
         *
         * Pacman does not reverse in the middle of path and ignores the path from which he came to the crossroad.
         * With probability of random_move_prob pacman does random move in cases 1 and 2.
         */

        /* pacman is not at crossroad => follow the path */
        if (!Utils.pacmanOnCrossroad(game)) {
            return Utils.pacmanFollowRoad(game);
        }

        /* Perform random move with probability of random_move_prob */
        if (random.nextDouble()<random_move_prob) {
            return Utils.randomPacmanMove(game, PACMAN_REVERSAL.XROADS_ONLY, random);
        }

        /* If any ghost is too close, try to flee */
        int pacman_position=game.getPacmanCurrentNodeIndex();
        for (GHOST ghost: GHOST.values()) {
            if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost)==0) {
                int ghost_position = game.getGhostCurrentNodeIndex(ghost);
                if (game.getShortestPathDistance(pacman_position, ghost_position)<MIN_DISTANCE) {
                    return game.getNextMoveAwayFromTarget(pacman_position, ghost_position, DM.PATH);
                }
            }
        }

        /* Hunt blue ghosts */
        int min_distance = Integer.MAX_VALUE;
        int closest_edible_ghost_position = -1;
        for (GHOST ghost: GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                int ghost_position = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(pacman_position, ghost_position);
                if (distance<min_distance) {
                    min_distance = distance;
                    closest_edible_ghost_position = ghost_position;
                }
            }
        }
        if (closest_edible_ghost_position!=-1) {
            return game.getNextMoveTowardsTarget(pacman_position,closest_edible_ghost_position,DM.PATH);
        }

        /* play a random move otherwise */
        return Utils.randomPacmanMove(game, PACMAN_REVERSAL.XROADS_ONLY, random);
    }

    private boolean pacmanNearPowerPill(Game game)
    {
    	int pacmanIndex=game.getPacmanCurrentNodeIndex();
    	int[] powerPillIndices=game.getActivePowerPillsIndices();

    	for(int i=0;i<powerPillIndices.length;i++)
    		if(game.getShortestPathDistance(powerPillIndices[i],pacmanIndex)<PILL_PROXIMITY)
    			return true;

        return false;
    }

    private MOVE legacyMove(Game game, GHOST ghost) {
        int pacman_position = game.getPacmanCurrentNodeIndex();
        int ghost_position = game.getGhostCurrentNodeIndex(ghost);
        MOVE last_move = game.getGhostLastMoveMade(ghost);
        DM dm = DM.PATH;
        switch (ghost) {
            /* case for BLINKY,SUE is implicit dm = DM.PATH (initialization of dm) */
            case INKY:
                dm = DM.MANHATTAN;
                break;
            case PINKY:
                dm = DM.EUCLID;
                break;
        }

        return game.getApproximateNextMoveAwayFromTarget(ghost_position, pacman_position, last_move, dm);
    }

    private EnumMap<GHOST, MOVE> chooseGhostsMoves(Game game) {
        /* Ghost strategy:
         * 1. If a ghost is edible or pacman is close to the power pill, run away
         * 2. Follow the midified Legacy strategy (no random moves for SUE, see getLegacyMove())
         *
         * With probability of random_move_prob ghost does random move.
         */

        EnumMap<GHOST, MOVE> ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        int pacman_position = game.getPacmanCurrentNodeIndex();
        boolean danger = pacmanNearPowerPill(game);

        for (GHOST ghost: GHOST.values()) {
            /* skip if move is not required */
            if (!game.doesGhostRequireAction(ghost)) {
                ghosts_moves.put(ghost, MOVE.NEUTRAL);
                continue;
            }

            /* with probability of random_move_prob, play a random move */
            if (random.nextDouble()<random_move_prob) {
                ghosts_moves.put(ghost, Utils.randomGhostsMove(game, ghost, random));
                continue;
            }

            int ghost_position = game.getGhostCurrentNodeIndex(ghost);
            MOVE last_ghost_move = game.getGhostLastMoveMade(ghost);
            /* if ghost is edible or pacman is close to power pill, run away */
            if (danger||game.isGhostEdible(ghost)) {
                ghosts_moves.put(ghost, game.getApproximateNextMoveAwayFromTarget(ghost_position, pacman_position, last_ghost_move, DM.PATH));
                continue;
            }

            /* follow the Legacy strategy */
            ghosts_moves.put(ghost, legacyMove(game, ghost));
        }

        return ghosts_moves;
    }

    private Moves chooseMoves(Game game) {
        MOVE pacman_move = choosePacmanMove(game);
        EnumMap<GHOST, MOVE> ghosts_moves = chooseGhostsMoves(game);

        return new Moves(pacman_move, ghosts_moves);
    }

    @Override
    public Pair<MCNode,Action> nodeStep(MCNode node) {
        if (node.pacmanOnTurn()) {
            MOVE pacman_move = choosePacmanMove(node.game);
            return new Pair<MCNode,Action>(node.child(pacman_move), new PacmanAction(pacman_move));
        } else if (node.ghostsOnTurn()) {
            EnumMap<GHOST, MOVE> ghosts_moves = chooseGhostsMoves(node.game);
            Utils.decisionMoves(ghosts_moves, node.game);
            return new Pair<MCNode,Action>(node.child(ghosts_moves), new GhostAction(ghosts_moves));
        } else {
            assert false;
            return null;
        }
    }

    @Override
    public void gameStep(Game game) {
        Moves moves = chooseMoves(game);
        game.advanceGameWithPowerPillReverseOnly(moves.pacmans, moves.ghosts.clone());
    }

    @Override
    public double simulate(Game game) {
        int max_score = MAX_SCORES[game.getCurrentLevel()];
        int max_pills = game.getNumberOfPills();

        Game simulation = game.copy();
        int current_level = game.getCurrentLevel();
        int depth = 0;

        while (!simulation.wasPacManEaten()&&simulation.getCurrentLevel()==current_level&&depth<max_depth) {
            Moves moves = chooseMoves(simulation);
            simulation.advanceGameWithPowerPillReverseOnly(moves.pacmans, moves.ghosts.clone());
            depth++;
        }

        int curr_pills = simulation.getNumberOfActivePills();

        return (1-death_weight)*((simulation.getScore()) / (double)max_score) + death_weight*(1)*(simulation.wasPacManEaten()? 0: 1);
    }
}
