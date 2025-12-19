package engine;

import core.AgentState;
import core.NodeAction;
import org.springframework.stereotype.Service;

@Service
public class GraphExecutor {

    private final GraphRegistry registry;

    public GraphExecutor(GraphRegistry registry) {
        this.registry = registry;
    }

    public AgentState run(AgentState initialState) {
        String currentNodeName = registry.getStartNode();
        AgentState currentState = initialState;

        System.out.println("--- Starting Graph Execution ---");

        while (currentNodeName != null) {
            System.out.println("Executing Node: " + currentNodeName);

            NodeAction node = registry.getNode(currentNodeName);
            if (node == null) break;

            // Execute the node
            currentState = node.execute(currentState);

            // Find next node
            // (Note: In a real framework, you'd handle conditional edges here)
            currentNodeName = registry.getNextNode(currentNodeName);
        }

        System.out.println("--- Graph Execution Finished ---");
        return currentState;
    }
}