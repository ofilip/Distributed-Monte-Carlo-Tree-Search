package mcts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mcts.Decision;
import mcts.Decision.DecisionCause;
import mcts.DecisionNeededFrom;
import mcts.Moves;
import mcts.Utils;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public abstract class MCNode implements UCBNode {
    /* MCTS values */
    int visit_count;
    double value;
    
    int ticks_to_go;
    
    public boolean dbg_halfstep = false;
    
    /* Tree links and values */
    MCTree tree;
    MCNode parent;    
    Map<MOVE, PacmanNode> pacman_children = null;
    Map<EnumMap<GHOST, MOVE>, GhostsNode> ghosts_children = null;
    int pacman_decision_gap; /* before how mant ticks happened last pacman decision */    
    DecisionCause decision_cause; /* NONE for "not set" */
    
    
    /* current game state */
    Game game; /* set only iff expanded()||isRoot() */
    
    public boolean expanded() {
        return pacman_children!=null||ghosts_children!=null;
    }
    
    /* Returns true if expanded() and next move is Pacman's */
    public boolean pacmanOnTurn() {
        return pacman_children!=null;
    }
    
    public boolean ghostsOnTurn() {
        return ghosts_children!=null;
    }
    
    public int ticksToGo() {
        return ticks_to_go;
    }
    
    public double simulate() {
        return tree.simulator.simulate(game);
    }
    
    public void backpropagate(double reward) {
        tree.backpropagator.backpropagate(this, reward);
    }
    
    public PacmanNode child(MOVE next_pacman_move) {
        if (pacman_children==null) {
            return null;
        }
        return pacman_children.get(next_pacman_move);
    }
    
    public GhostsNode child(EnumMap<GHOST, MOVE> next_ghosts_moves) {
        if (ghosts_children==null) {
            return null;
        }
        GhostsNode node = ghosts_children.get(next_ghosts_moves);
        if (node==null) {
            /* power pill was eaten while some ghost is on crossroad, all ghosts
             * forced to reverse so all branches are the same. */
            assert game.wasPowerPillEaten();
            assert Utils.ghostOnCrossroad(game);
            node = ghosts_children.values().iterator().next();
        }
        return node;
    }
    
    public MCNode selectNext() {
        return (MCNode)tree.selector.select(this);
    }
    
    public MCNode select() {
        if (visit_count==0) {
            return this;
        } else {
            return selectNext().select();
        }
    }
    
    public double value() {
        return value;
    }
    
    public int pacmanDecisionGap() {
        return pacman_decision_gap;
    }
    
    private MCNode pacmanBestMove() {
        MCNode best = null;
        double best_val = Double.NEGATIVE_INFINITY;
        
        for (MCNode child: pacman_children.values()) {
            double curr_val = child.value();
            
            if (curr_val>best_val) {
                best_val = curr_val;
                best = child;
            }
        }
        
        return best;
    }
    
    private MCNode ghostsBestMove() {
        MCNode best = null;
        double best_val = Double.NEGATIVE_INFINITY;
        
        for (MCNode child: ghosts_children.values()) {
            double curr_val = -child.value();
            
            if (curr_val>best_val) {
                best_val = curr_val;
                best = child;
            }
        }
        
        return best;
    }
    
    /** 
     * Node best to play by the player 
     */
    public MCNode bestMove() {
        if (pacmanOnTurn()) {
            return pacmanBestMove();
        } else {
            assert ghostsOnTurn();
            return ghostsBestMove();
        }
    }
    
    public boolean isRoot() {
        return parent==null;
    }
    
    @Override
    public int visitCount() {
        return this.visit_count;
    }   
    
    protected MCNode(MCTree tree, MCNode parent, Game game, int initial_ticks, int pacman_decision_gap) {
        this.tree = tree;
        this.parent = parent;
        this.game = game;
        this.ticks_to_go = initial_ticks;
        this.pacman_decision_gap = pacman_decision_gap;
        this.decision_cause = DecisionCause.NONE;
    }
    
    public Iterable<? extends MCNode> children() {
        if (pacmanOnTurn()) {
            return pacman_children.values();
            
        } else if (ghostsOnTurn()) {
            return ghosts_children.values();
        } else {
            return null;
        }
    }
            
    public Game game() {
        return game;
    }
    
    public DecisionCause decisionCause() {
        return decision_cause;
    }
        
    protected void pacmanExpand(Decision decision) {
        MOVE[] possible_pacman_moves = decision.pacman_possible_moves;
        assert !expanded();        
        pacman_children = new EnumMap<MOVE, PacmanNode>(MOVE.class);
        MOVE[] pacman_moves = possible_pacman_moves;
        decision_cause = decision.pacman_decision_cause;
        for (MOVE possible_pacman_move: pacman_moves) {
            pacman_children.put(possible_pacman_move, PacmanNode.createUnvisitedNode(tree, this, possible_pacman_move, decision.pacman_decision_gap));
        }
    }
    
    protected void ghostsExpand(Decision decision) {
        EnumMap<GHOST, MOVE[]> possible_ghosts_moves = decision.ghosts_possible_moves;
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
                        Utils.decisionMoves(ground_ghosts_moves, decision.game);
                        ghosts_children.put(ground_ghosts_moves, GhostsNode.createUnvisitedNode(tree, this, ground_ghosts_moves, decision.pacman_decision_gap));
                    }
                }
            }
        }   
    }
    
    protected abstract void jointExpand(Decision decision);
    
    /* Advances game accordingly to the direction of all agents.
     * Player's agent uses the move from node.
     * Opponents follow their path.
     */
    abstract protected void advanceGame(Game game);
    
    public final void expand() {
        if (expanded()) {
            return;
        }
        
        Decision decision;
        if (game==null) {
            /* Game not set => create game by advancing game until decision is required */
            assert dbg_halfstep==false;
            game = parent.game.copy();
            // XXX TODO: What if this.joint_node==true? (
            advanceGame(game);
            decision = Decision.nextDecision(game, (pacman_decision_gap+1)%Decision.PACMAN_DECISION_GAP, true);
            game = decision.game;
            decision.ticks++; /* +1 for advanceGame(game) */
            ticks_to_go = decision.ticks;
        } else if (isRoot()) {
            decision = Decision.nextDecision(game, pacman_decision_gap, false);
        } else {
            /* Game already set => calculate possible moves using Decision object 
             * This case happens only if node is created using createJointNode() */
            assert parent.dbg_halfstep;
            decision = Decision.nextDecision(game, pacman_decision_gap, true);
        }
        
        if (decision.jointDecision()) {
            
            jointExpand(decision);
        } else if (decision.pacmansDecision()) {
            pacmanExpand(decision);
        } else {
            assert decision.ghostsDecision();
            ghostsExpand(decision);
        }
    }
    
    public String toString(int depth_limit) {
        return this.toString(new StringBuilder(), 0, depth_limit).toString();
    }

    protected abstract StringBuilder movesToString(StringBuilder result);
    protected abstract StringBuilder typeToString(StringBuilder result);
    
    protected StringBuilder toString(StringBuilder result, int depth, int depth_limit)  {
        for (int i=0; i<depth; i++) {
            result.append('\t');
        }
        
        result.append("[");
        typeToString(result);
        result.append("|");
        result.append(decision_cause.shortName());
        result.append("|");
        result.append(pacman_decision_gap);
        result.append("]");
        if (this.isRoot()) {
            result.append("/ROOT");
        }
        if (this.dbg_halfstep) {
            result.append("/halfstep");
        }
        result.append(" c=").append(this.visit_count)
                .append(" t=").append(this.ticks_to_go)
                .append(" v=").append(this.value());


        movesToString(result);
        result.append(")");
        if (game!=null&&game.wasPacManEaten()&&!isRoot()) {
            result.append(" /pacman eaten/\n");
        } else {        
            if (depth_limit<=depth&&expanded()) {
                result.append(" /subtree hidden/\n");
            } else {
                result.append("\n");
                if (expanded()&&depth_limit>depth) {
                    for (MCNode child: children()) {
                        child.toString(result, depth+1, depth_limit);
                    }
                }
            }
        }
        
        
        return result;
    }
    
    @Override
    public String toString() {
        return toString(Integer.MAX_VALUE);
    }
    
    public abstract boolean isPacmanNode();
    public boolean isGhostsNode() {
        return !isPacmanNode();
    }
}
