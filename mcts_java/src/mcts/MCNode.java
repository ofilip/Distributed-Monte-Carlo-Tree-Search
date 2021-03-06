package mcts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import utils.Pair;

public abstract class MCNode implements UCBNode {
    /* MCTS values */
    int visit_count;
    int calculated_visit_count;
    double value;
    double calculated_value;

    Map<GHOST,Integer> received_visit_count = new EnumMap<GHOST,Integer>(GHOST.class);
    Map<GHOST,Double> received_value = new EnumMap<GHOST,Double>(GHOST.class);
    {
        for (GHOST ghost: GHOST.values()) {
            received_visit_count.put(ghost, 0);
            received_value.put(ghost, 0.0);
        }
    }

    int ticksToGo;
    long totalTicks; /* ticks from original root (before any updateTree() call */

    protected boolean halfstep = false;
//    protected boolean jointNode = false;

    /* Tree links and values */
    MCTree tree;
    MCNode parent;
    Map<MOVE, PacmanNode> pacman_children = null;
    Map<EnumMap<GHOST, MOVE>, GhostsNode> ghosts_children = null;
    int pacman_decision_gap; /* before how mant ticks happened last pacman decision */
    DecisionCause decision_cause = DecisionCause.NONE; /* NONE for "not set" */
    boolean terminal = false;

    /* current game state */
    Game game; /* set only iff expanded()||isRoot() */

//    protected MCNode copy(MCTree tree, MCNode parent) {
//        return copy(tree, parent, -1);
//    }
//    protected abstract MCNode copy(MCTree tree, MCNode parent, long depth);

//    protected MCNode(MCTree tree, MCNode node, MCNode parent, long depth) {
//        this.tree = tree;
//        this.parent = parent;
//        this.game = node.game.copy();
//        this.pacman_decision_gap = node.pacman_decision_gap;
//        this.decision_cause = node.decision_cause;
//        if (depth!=0) {
//            if (node.pacman_children!=null) {
//                this.pacman_children = new EnumMap<MOVE, PacmanNode>(MOVE.class);
//                for (Entry<MOVE, PacmanNode> e: pacman_children.entrySet()) {
//                    this.pacman_children.put(e.getKey(), (PacmanNode)e.getValue().copy(tree, this, Math.max(-1, depth-1)));
//                }
//            }
//            if (node.ghosts_children!=null) {
//                for (Entry<EnumMap<GHOST, MOVE>, GhostsNode> e: ghosts_children.entrySet()) {
//                    this.ghosts_children.put(e.getKey().clone(), (GhostsNode)e.getValue().copy(tree, this, Math.max(-1, depth-1)));
//                }
//            }
//        } else {
//            this.expand(); /* leaves should be always expanded */
//        }
//    }

    protected MCNode(MCTree tree, MCNode parent, Game game, int initial_ticks, int pacman_decision_gap, long totalTicks) {
        this.tree = tree;
        this.parent = parent;
        this.game = game;
        this.ticksToGo = initial_ticks;
        this.pacman_decision_gap = pacman_decision_gap;
        this.decision_cause = DecisionCause.NONE;
        this.totalTicks = totalTicks;
    }

    public boolean expanded() {
        return pacman_children!=null||ghosts_children!=null;
    }

    public long getTotalTicks() {
        return totalTicks;
    }

    /* Returns true if expanded() and next move is Pacman's */
    public boolean pacmanOnTurn() {
        return pacman_children!=null;
    }

    public boolean ghostsOnTurn() {
        return ghosts_children!=null;
    }

    public int ticksToGo() {
        return ticksToGo;
    }

    public double simulate() {
        return tree.simulator.simulate(game, totalTicks);
    }



    public void backpropagate(double reward) { backpropagate(reward, 1); }

    public void backpropagate(double reward, int count) {
        tree.backpropagator.backpropagate(this, reward, count);
    }

    public long backpropagateReceived(GHOST from, double reward, int count) {
        long res = tree.backpropagator.backpropagateReceived(this, from, reward, count);
        return res;
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

    public MCNode child(Action action) {
        if (action.type()==Action.Type.PACMAN) {
            return child(action.pacmanMove());
        } else {
            return child(action.ghostMove());
        }
    }

    public MCNode parent() { return parent; }

    public MCNode selectNext() {
        return selectNextNodeActionPair().first;
    }

    private Pair<MCNode,Action> selectNextNodeActionPair() {
        return tree.selector.select(this);
    }

    public MCNode select() {
        if (visit_count==0||terminal) {
            return this;
        } else {
            return selectNext().select();
        }
    }

    public MCNode select(List<Action> action_list) {
        if (visit_count==0||terminal) {
            return this;
        } else {
            Pair<MCNode,Action> selected = selectNextNodeActionPair();
            assert(selected!=null);
            action_list.add(selected.second);
            return selected.first.select(action_list);
        }
    }

    public double value() {
        return value;
    }

    public double calculatedValue() {
        return calculated_value;
    }

//    public Pair<Double,Integer> calculated() {
//        double val = value;
//        int cnt = visit_count;
//        for (GHOST ghost: GHOST.values()) {
//            cnt -= received_visit_count.get(ghost);
//            val = (val*cnt - received_value.get(ghost)*received_visit_count.get(ghost))/cnt;
//        }
//        return new Pair<Double,Integer>(val,cnt);
//    }

    public boolean halfstep() {
        return halfstep;
    }

    public boolean halfstepFollows() {
        Iterator<? extends MCNode> it = children().iterator();
        return it.hasNext()&&it.next().halfstep;
    }

//    public boolean jointNode() {
//        return jointNode;
//    }
//
//    public boolean jointNodeFollows() {
//        Iterator<? extends MCNode> it = children().iterator();
//        return it.hasNext()&&it.next().jointNode;
//    }

    public int pacmanDecisionGap() {
        return pacman_decision_gap;
    }

    /**
     * Node best to play by the player
     */
    public MCNode bestMove() {
        MCNode best = null;

        if (children()==null) {
            return null;
        }

        for (MCNode child: children()) {
            if (best==null||child.visitCount()>best.visitCount()) {
                best = child;
            }
        }

        return best;
    }

    public boolean isRoot() {
        return parent==null;
    }

    @Override
    public int visitCount() {
        return this.visit_count;
    }

    public int calculatedVisitCount() {
        return this.calculated_visit_count;
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

    public Map<MOVE, PacmanNode> pacmanChildren() {
        return pacman_children;
    }

    public Map<EnumMap<GHOST,MOVE>, GhostsNode> ghostsChildren() {
        return ghosts_children;
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
            pacman_children.put(possible_pacman_move, PacmanNode.createUnvisitedNode(tree, this,
                                possible_pacman_move, decision.pacman_decision_gap, totalTicks+decision.ticks));
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
                        ghosts_children.put(ground_ghosts_moves, GhostsNode.createUnvisitedNode(tree, this, ground_ghosts_moves, decision.pacman_decision_gap,
                                                                                                this.totalTicks+decision.ticks));
                    }
                }
            }
        }
    }

    private void pacmanGhostsExpand(Decision decision) {
        MOVE[] possible_pacman_moves = decision.pacman_possible_moves;
        EnumMap<GHOST, MOVE[]> possible_ghosts_moves = decision.ghosts_possible_moves;
        assert !expanded();
        pacman_children = new EnumMap<MOVE, PacmanNode>(MOVE.class);
        decision_cause = decision.pacman_decision_cause;
        for (MOVE pacman_move: possible_pacman_moves) {
            pacman_children.put(pacman_move, PacmanNode.createJointNode(tree, this, pacman_move, possible_ghosts_moves,
                                decision.game, decision.pacman_decision_gap, totalTicks+decision.ticks));
        }
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
                                GhostsNode.createJointNode(tree, this, ground_ghosts_moves, possible_pacman_moves,
                                                           decision.pacman_decision_gap, decision.pacman_decision_cause, totalTicks+decision.ticks));
                    }
                }
            }
        }
    }

    protected void jointExpand(Decision decision) {
        if (tree.isPacmanTree()) {
            if (tree.getOptimisticTurns()) {
                ghostsPacmanExpand(decision);
            } else {
                pacmanGhostsExpand(decision);
            }
        } else {
            if (tree.getOptimisticTurns()) {
                pacmanGhostsExpand(decision);
            } else {
                ghostsPacmanExpand(decision);
            }
        }
    }

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
            assert halfstep==false;
            game = parent.game.copy();
            advanceGame(game);
            decision = Decision.nextDecision(game, (pacman_decision_gap+1)%Decision.PACMAN_DECISION_GAP, true);
            game = decision.game;
            decision.ticks++; /* +1 for advanceGame(game) */
            ticksToGo = decision.ticks;
        } else if (isRoot()) {
            decision = Decision.nextDecision(game, pacman_decision_gap, false);
        } else {
            /* Game already set => calculate possible moves using Decision object
             * This case happens only if node is created using createJointNode() */
            assert parent.halfstep;
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
        if (this.halfstep) {
            result.append("/halfstep");
        }

        StringBuilder rec = new StringBuilder();

        for (GHOST ghost: received_visit_count.keySet()) {
            Integer vc = received_visit_count.get(ghost);
            if (vc>0) {
                rec.append(" ").append(ghost.toString().charAt(0)).append("{").append(vc).append("/").append(received_value.get(ghost)).append("}");
            }
        }

        result.append(" c=").append(this.visit_count)
                .append("(").append(this.calculated_visit_count).append(")")
                .append(" t=").append(this.ticksToGo)
                .append(" v=").append(this.value())
                .append("(").append(this.calculated_value).append(")");
        result.append(rec);

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

    /**
     * @return the received_visit_count
     */
    public int getReceivedVisitCount(GHOST from) {
        return received_visit_count.get(from);
    }

    /**
     * @return the received_value
     */
    public double getReceivedValue(GHOST from) {
        return received_value.get(from);
    }
}
