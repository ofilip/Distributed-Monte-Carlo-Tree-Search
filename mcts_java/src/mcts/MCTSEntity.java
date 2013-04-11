package mcts;

import utils.VerboseLevel;

public interface MCTSEntity {

    public abstract VerboseLevel getVerboseLevel();
    public abstract double getUcbCoef();
    public abstract double getDeathWeight();
    public abstract int getSimulationDepth();
    public abstract double getRandomSimulationMoveProbability();

    public abstract void setVerboseLevel(VerboseLevel verboseLevel);
    public abstract void setUcbCoef(double ucbCoef);
    public abstract void setDeathWeight(double deathWeight);
    public abstract void setSimulationDepth(int simulationDepth);
    public abstract void setRandomSimulationMoveProbability(double randomSimulationMoveProbability);
}
