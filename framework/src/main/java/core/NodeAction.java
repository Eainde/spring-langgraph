package core;

public interface NodeAction {
    /**
     * Executes the node logic.
     * @param state The current state of the graph.
     * @return The updated state.
     */
    AgentState execute(AgentState state);
}