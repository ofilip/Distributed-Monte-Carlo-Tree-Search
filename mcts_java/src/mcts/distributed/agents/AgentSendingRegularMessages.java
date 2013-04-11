package mcts.distributed.agents;

public interface AgentSendingRegularMessages {
    public long getMessageInterval();
    public void setMessageInterval(long interval);
}
