package communication.messages;

import java.util.*;
import mcts.Action;
import mcts.Utils;
import pacman.game.Constants.*;

public class SimulationResultMessage extends Message {
    private List<Action> tree_moves; /* Moves defining node where the simulation began */
    private double simulation_result;
    
    public SimulationResultMessage(List<Action> tree_moves, double simulation_result) {
        super("simulation_result");
        this.tree_moves = tree_moves;
        this.simulation_result = simulation_result;
    }
    
    public List<Action> treeMoves() { return tree_moves; }
    public double simulationResult() { return simulation_result; }
    
    @Override
    public long length() {
        long bits_length = 8; /* size of double */
        
        for (Action action: tree_moves) {
            bits_length += action.type().bitLength();
        }
        
        return Utils.bitsToBytes(bits_length);
    }

}
