package dsl;

import state.AgentState;

// The Contract for defining a Workflow
public interface WorkflowDefinition<S extends AgentState> {
    String getWorkflowId(); // Unique ID for tracking
    String getVersion();    // Version string (e.g., "1.0.0")

    void define(FlowBuilder<S> builder);
    Class<S> getStateType();
}
