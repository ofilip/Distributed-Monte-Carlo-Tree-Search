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
import utils.Pair;

/**
 * Simulation based on ideas (simplified) described in 
 * 'Nozomu Ikehata and Takeshi Ito: Monte-Carlo Tree Search in Ms. Pac-Man'
 */
public class MySimulator implements Simulator {
    private int max_depth;
    private Random random;

    public static double sigm(double x) {
        return 1/(1+Math.exp(-x));
    }
    
    public MySimulator(int max_depth, long seed) {
        this.random = new Random(seed);
        this.max_depth = max_depth;
    }
    
    private MOVE choosePacmanMove(Game game) {
        MOVE pacman_move;
        
        if (Utils.pacmanOnCrossroad(game)) {
            pacman_move = Utils.randomPacmanMove(game, PACMAN_REVERSAL.XROADS_ONLY, random);
        } else {
            pacman_move = Utils.getPacmansPossibleMoves(game, PACMAN_REVERSAL.NEVER)[0];            
        }
        
        return pacman_move;
    }
    
    private EnumMap<GHOST, MOVE> chooseGhostsMoves(Game game) {
        
        EnumMap<GHOST, MOVE> ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        int pacman_pos = game.getPacmanCurrentNodeIndex();
        
        for (GHOST ghost: GHOST.values()) {
            int ghost_pos = game.getGhostCurrentNodeIndex(ghost);
            MOVE ghost_move;
            
            //XXX: consider blue ghosts!
            if (random.nextDouble()<0.9) {
                if (Utils.ghostOnCrossroad(game, ghost)) {
                    MOVE last_move = game.getGhostLastMoveMade(ghost);
                    MOVE move = game.getApproximateNextMoveTowardsTarget(ghost_pos, pacman_pos, last_move, DM.PATH);
                    ghost_move = move==null? MOVE.NEUTRAL: move;
                } else {
                    ghost_move = Utils.getGhostsPossibleMoves(game, ghost)[0];                    
                }
            } else {
                ghost_move = Utils.randomGhostsMove(game, ghost, random);
            }
            ghosts_moves.put(ghost, ghost_move);
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
        Game simulation = game.copy();
        int current_level = game.getCurrentLevel();
        int depth = 0;
        
        while (!simulation.wasPacManEaten()&&simulation.getCurrentLevel()==current_level&&depth<max_depth) {
            Moves moves = chooseMoves(simulation);
            simulation.advanceGameWithPowerPillReverseOnly(moves.pacmans, moves.ghosts.clone());
            depth++;
        }
        
        return simulation.getScore() / 3000.0;
    }

}
