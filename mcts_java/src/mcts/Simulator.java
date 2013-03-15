package mcts;

import pacman.game.Game;
import utils.Pair;

public interface Simulator {
    public Pair<MCNode,Action> nodeStep(MCNode node);
    public void gameStep(Game g);
    public double simulate(Game g);
}
