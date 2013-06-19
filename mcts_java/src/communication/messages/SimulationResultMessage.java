package communication.messages;

import java.util.*;
import mcts.Action;
import mcts.Utils;
import pacman.game.Constants.*;

public class SimulationResultMessage extends Message {
    private List<Action> treeMoves; /* Moves defining node where the simulation began */
    private double simulationResult;
    private long length = -1;

    public SimulationResultMessage(List<Action> treeMoves, double simulationResult) {
        super("simulation_result");
        this.treeMoves = treeMoves;
        this.simulationResult = simulationResult;
    }

    public List<Action> treeMoves() { return treeMoves; }
    public double simulationResult() { return simulationResult; }

    @Override
    public long length() {
        if (length==-1) {
            long bitsLength = 32; /* size of double */

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
        sb.append(String.format("[%s] val:%.2f, bytes: %s, path:", this.hashCode(), simulationResult, length()));
        for (Action action: treeMoves) {
            sb.append(' ');
            sb.append(action.toString());
        }
        return sb.toString();
    }
}
