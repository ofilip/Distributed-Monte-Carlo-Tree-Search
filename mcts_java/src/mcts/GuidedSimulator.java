package mcts;

import java.util.EnumMap;
import java.util.Random;
import mcts.Moves;
import mcts.Utils;
import mcts.Utils.PACMAN_REVERSAL;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.FullGame;
import pacman.game.Game;
import pacman.game.GameView;
import utils.Pair;

/**
 * Simulator guided by simplified starter controllers.
 * Simulation based on ideas (simplified) described in
 * 'Nozomu Ikehata and Takeshi Ito: Monte-Carlo Tree Search in Ms. Pac-Man',
 * StarterPacMan and the Legacy ghosts.
 */
public class GuidedSimulator {
    public final static int MIN_DISTANCE = 20;
    public final static int PILL_PROXIMITY = 15;

    public final static int[] MAX_SCORES = new int[4];
    public final static int[] MAX_PILLS = new int[4];

    static {
        for (int i=0; i<4; i++) {
            Game game = new FullGame(0, i);
            MAX_SCORES[i] = 12000 + 200 + 10*game.getPillIndices().length;
        }
    }

    private double randomMoveProb = Constants.DEFAULT_RANDOM_PROB;
    private int simulationDepth = Constants.DEFAULT_SIMULATION_DEPTH;
    private double deathWeight = Constants.DEFAULT_DEATH_WEIGHT;

    private Random random;

    public static double sigm(double x) {
        return 1/(1+Math.exp(-x));
    }


    public GuidedSimulator(Random random) {
        this.random = random;
    }

    private MOVE choosePacmanMove(Game game) {
        /* Simplified strategy of StarterPacMan:
         * 1. if a ghost is too close, then pacman tries to flee
         * 2. if a blue ghost is nearby, pacman hunts it
         * 3. otherwise pacman choose random way
         *
         * Pacman does not reverse in the middle of path and ignores the path from which he came to the crossroad.
         * With probability of randomMoveProb pacman does random move in cases 1 and 2.
         */

        /* pacman is not at crossroad => follow the path */
        if (!Utils.pacmanOnCrossroad(game)) {
            return Utils.pacmanFollowRoad(game);
        }

        /* Perform random move with probability of randomMoveProb */
        if (random.nextDouble()<getRandomMoveProb()) {
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
         * With probability of randomMoveProb ghost does random move.
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

            /* with probability of randomMoveProb, play a random move */
            if (random.nextDouble()<getRandomMoveProb()) {
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

    public void gameStep(Game game) {
        Moves moves = chooseMoves(game);
        game.advanceGameWithPowerPillReverseOnly(moves.pacmans, moves.ghosts.clone());
    }

    public double simulate(Game game, long startDepth) {
        int max_score = MAX_SCORES[game.getCurrentLevel()%4];

        Game simulation = game.copy();
        int current_level = game.getCurrentLevel();
        long depth = startDepth;

        while (!simulation.wasPacManEaten()&&simulation.getCurrentLevel()==current_level&&depth<getMaxDepth()) {
            Moves moves = chooseMoves(simulation);
            simulation.advanceGameWithPowerPillReverseOnly(moves.pacmans, moves.ghosts.clone());
            depth++;
        }

        return (1-getDeathWeight())*((simulation.getScore()) / (double)max_score) + getDeathWeight()*(simulation.wasPacManEaten()? 0: 1);
    }

    /**
     * @return the randomMoveProb
     */
    public double getRandomMoveProb() {
        return randomMoveProb;
    }

    /**
     * @param randomMoveProb the randomMoveProb to set
     */
    public void setRandomMoveProb(double randomMoveProb) {
        this.randomMoveProb = randomMoveProb;
    }

    /**
     * @return the maxDepth
     */
    public int getMaxDepth() {
        return simulationDepth;
    }

    /**
     * @param maxDepth the maxDepth to set
     */
    public void setMaxDepth(int maxDepth) {
        this.simulationDepth = maxDepth;
    }

    /**
     * @return the deathWeight
     */
    public double getDeathWeight() {
        return deathWeight;
    }

    /**
     * @param deathWeight the deathWeight to set
     */
    public void setDeathWeight(double deathWeight) {
        this.deathWeight = deathWeight;
    }
}
