package mcts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.Decision;
import mcts.Decision.DecisionCause;
import mcts.Moves;
import mcts.Utils;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PacmanNode extends MCNode {
    MOVE pacman_move;
    
    @Override
    protected MCNode copy(MCTree tree, MCNode parent, long depth) {
        return new PacmanNode(tree, this, parent, depth);
    }
    
    protected PacmanNode(MCTree tree, PacmanNode node, MCNode parent, long depth) {
        super(tree, node, parent, depth);
        this.pacman_move = node.pacman_move;
    }
    
    protected PacmanNode(MCTree tree, MCNode parent, Game game, MOVE pacman_move, int initial_ticks, int pacman_decision_gap) {
        super(tree, parent, game, initial_ticks, pacman_decision_gap);
        this.pacman_move = pacman_move;
    }
    
    protected PacmanNode(MCTree tree, MCNode parent, Game parent_game, MOVE pacman_move, EnumMap<GHOST, MOVE[]> possible_ghosts_moves, Game game, int pacman_decision_gap) {
        this(tree, parent, parent_game.copy(), pacman_move, 0, pacman_decision_gap);
        //TODO: 
//        this.game.updatePacMan(pacman_move);
//        this.game.updateGame();
        this.halfstep = true;
        decision_cause = DecisionCause.CROSSROAD_REACHED;
        ghosts_children = new HashMap<EnumMap<GHOST, MOVE>, GhostsNode>();
        MOVE[] blinky_moves = possible_ghosts_moves.get(Constants.GHOST.BLINKY);        
        MOVE[] pinky_moves = possible_ghosts_moves.get(Constants.GHOST.PINKY);
        MOVE[] inky_moves = possible_ghosts_moves.get(Constants.GHOST.INKY);
        MOVE[] sue_moves = possible_ghosts_moves.get(Constants.GHOST.SUE);
        EnumMap<GHOST, MOVE> ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        for (Constants.MOVE blinky_move: blinky_moves) {
            ghosts_moves.put(GHOST.BLINKY, blinky_move);
            for (Constants.MOVE inky_move: inky_moves) {                 
                ghosts_moves.put(GHOST.INKY, inky_move);
                for (Constants.MOVE pinky_move: pinky_moves) {
                    ghosts_moves.put(GHOST.PINKY, pinky_move);
                    for (Constants.MOVE sue_move: sue_moves) {                        
                        ghosts_moves.put(GHOST.SUE, sue_move);  
                        EnumMap<GHOST, MOVE> ground_ghosts_moves = ghosts_moves.clone();
                        Utils.decisionMoves(ground_ghosts_moves, game);
                        Game child_game = parent_game.copy();
                        child_game.advanceGameWithPowerPillReverseOnly(pacman_move, ground_ghosts_moves.clone());
                        Decision decision = Decision.nextDecision(child_game, pacman_decision_gap);
                        decision.ticks++;
                        ghosts_children.put(ground_ghosts_moves, 
                                GhostsNode.createUnvisitedNode(tree, this, ground_ghosts_moves, 
                                                                decision.game, decision.ticks, decision.pacman_decision_gap));
                    }
                }
            }
        } 
    }
    
    static PacmanNode createUnvisitedNode(MCTree tree, MCNode parent, MOVE pacman_move, int pacman_decision_gap) {
        return createUnvisitedNode(tree, parent, pacman_move, null, 0, pacman_decision_gap);
    }
    
    static PacmanNode createUnvisitedNode(MCTree tree, MCNode parent, MOVE pacman_move, Game game, int initial_ticks, int pacman_decision_gap) {
        return new PacmanNode(tree, parent, game, pacman_move, initial_ticks, pacman_decision_gap);
    }
    
    static PacmanNode createRoot(MCTree tree, Game game) {
        Decision decision = Decision.nextDecision(game, 0);
        PacmanNode root = new PacmanNode(tree, null, decision.game, null, decision.ticks, decision.pacman_decision_gap);
        root.visit_count = 1;
        root.expand();
        return root;
    }
    
    /**
     * Joint node means that the node has children with moves played simultaneously in game (but move in the node
     * is played first (before children turns) for selection purposes. The node is immediately expanded
     * @param pacmans_moves Moves for node's children.
     * @param game For purposes of decisionMoves only.
     */
    static PacmanNode createJointNode(MCTree tree, MCNode parent,  MOVE pacman_move, EnumMap<GHOST, MOVE[]> possible_ghosts_moves, Game game, int pacman_decision_gap) {
        PacmanNode node =  new PacmanNode(tree, parent, parent.game, pacman_move, possible_ghosts_moves, game, pacman_decision_gap);
        return node;
    }
    
    private void ghostsPacmanExpand(Decision decision) {        
        EnumMap<GHOST, MOVE[]> possible_ghosts_moves = decision.ghosts_possible_moves;
        MOVE[] possible_pacman_moves = decision.pacman_possible_moves;
        assert !expanded();
        ghosts_children = new HashMap<EnumMap<GHOST, MOVE>, GhostsNode>();
        decision_cause = DecisionCause.CROSSROAD_REACHED;
        MOVE[] blinky_moves = possible_ghosts_moves.get(GHOST.BLINKY);
        MOVE[] inky_moves = possible_ghosts_moves.get(GHOST.INKY);
        MOVE[] pinky_moves = possible_ghosts_moves.get(GHOST.PINKY);
        MOVE[] sue_moves = possible_ghosts_moves.get(GHOST.SUE);
        EnumMap<GHOST, MOVE> ghosts_moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        for (MOVE blinky_move: blinky_moves) {
            ghosts_moves.put(GHOST.BLINKY, blinky_move);
            for (MOVE inky_move: inky_moves) {                 
                ghosts_moves.put(GHOST.INKY, inky_move);
                for (MOVE pinky_move: pinky_moves) {
                    ghosts_moves.put(GHOST.PINKY, pinky_move);
                    for (MOVE sue_move: sue_moves) {                        
                        ghosts_moves.put(GHOST.SUE, sue_move);  
                        EnumMap<GHOST, MOVE> ground_ghosts_moves = ghosts_moves.clone();
                        Utils.decisionMoves(ground_ghosts_moves, game);
                        ghosts_children.put(ground_ghosts_moves, 
                                GhostsNode.createJointNode(tree, this, 
                                        ground_ghosts_moves, possible_pacman_moves, decision.pacman_decision_gap, decision.pacman_decision_cause));
                    }
                }
            }
        }
    }

    @Override
    protected void jointExpand(Decision decision) {
        ghostsPacmanExpand(decision);
    }
    
    @Override
    protected StringBuilder movesToString(StringBuilder result) {
        result.append(" PACMAN=").append(this.pacman_move);
        return result;
    }
    
    @Override
    protected StringBuilder typeToString(StringBuilder result) {
        result.append("PACMAN");
        return result;
    }
        
    public MOVE pacmanMove() {
        return pacman_move;
    }
    
    @Override
    public boolean isPacmanNode() {
        return true;
    }

    @Override
    protected void advanceGame(Game game) {
        EnumMap<GHOST, MOVE> ghosts_moves = Utils.ghostsFollowRoads(game);
        game.advanceGameWithPowerPillReverseOnly(pacman_move, ghosts_moves/*.clone() not necessary*/);
    }
    
    @Override
    public double ucbValue() {
        assert !isRoot();
        
        return Utils.UCB1(this.value(), parent.visit_count, this.visit_count, tree.ucb1_coef);
    }
}
