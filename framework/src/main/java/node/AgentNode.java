package node;

import state.AgentState;

@FunctionalInterface
public interface AgentNode<S extends AgentState> {
    S execute(S state);
}
