package mcts;

import java.util.List;
import utils.Pair;

public interface Selector {
    public Pair<MCNode,Action> select(MCNode node);
}
