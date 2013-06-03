package communication.messages;

import java.util.*;
import mcts.Action;
import mcts.Utils;
import pacman.game.Constants.*;

public class TreeNodeMessage extends Message {
    private List<Action> treeMoves; /* Moves defining node where the simulation began */
    private double simulationResult;
    private int count;
    private long length = -1;

    public TreeNodeMessage(List<Action> treeMoves, double simulationResult, int count) {
        super("tree_node");
        this.treeMoves = treeMoves;
        this.simulationResult = simulationResult;
        this.count = count;
    }

    public List<Action> treeMoves() { return treeMoves; }
    public double simulationResult() { return simulationResult; }
    public int count() { return count; }

    @Override
    public long length() {
        if (length==-1) {
            long bitsLength = 10; /* size of double + sizeof short */

            for (Action action: treeMoves) {
                bitsLength += action.type().bitLength();
            }

            length = Utils.bitsToBytes(bitsLength);
        }
        return length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.name);
        sb.append(String.format("[%s] val:%s, count: %d, bytes: %s, path:", this.hashCode(), simulationResult, count, length()));
        for (Action action: treeMoves) {
            sb.append(' ');
            sb.append(action.toString());
        }
        return sb.toString();
    }
}
