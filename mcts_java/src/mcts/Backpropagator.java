package mcts;

import pacman.game.Constants.GHOST;

public interface Backpropagator {
    public void backpropagate(MCNode node, double reward, int count);
    public long backpropagateReceived(MCNode node, GHOST from, double reward, int count);
}
