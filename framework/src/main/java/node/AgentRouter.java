package node;

import state.AgentState;

// The Developer Interface for Routing logic
@FunctionalInterface
public interface AgentRouter<S extends AgentState> {
    String route(S state);
}
