package core;

/**
 * Functional interface representing the business logic of a graph node.
 * <p>
 * Any Spring Bean annotated with {@link com.springgraph.annotations.LangNode}
 * must implement this interface.
 * </p>
 */
public interface NodeAction {
    /**
     * Executes the business logic for this step in the workflow.
     *
     * @param state The current snapshot of the agent's memory/state.
     * @return The updated state to be passed to the next node.
     */
    AgentState execute(AgentState state);
}