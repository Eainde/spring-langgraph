package core;

public interface ConditionEvaluator {
    /**
     * @param state The current state of the agent
     * @return The name of the next node (target)
     */
    String determineNextNode(AgentState state);
}