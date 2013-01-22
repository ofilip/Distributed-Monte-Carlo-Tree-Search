package mcts;

import pacman.game.Game;

public interface Simulator {
    public MCNode nodeStep(MCNode node);
    public void gameStep(Game g);
    public double simulate(Game g);
}
