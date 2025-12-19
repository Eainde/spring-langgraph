package core;

/**
 * Strategy interface for determining dynamic transitions between nodes.
 * <p>
 * Implementations of this interface are used by {@link com.springgraph.annotations.LangConditionalEdge}
 * to perform "Router" logic. It analyzes the {@link AgentState} and returns the name
 * of the next node to execute.
 * </p>
 */
public interface ConditionEvaluator {
    /**
     * Analyzes the current state to decide the next path.
     *
     * @param state The state containing results from the previous node.
     * @return The name (String ID) of the next node to transition to.
     */
    String determineNextNode(AgentState state);
}