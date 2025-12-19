package engine;

import core.AgentState;
import core.NodeAction;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The runtime engine responsible for traversing the graph.
 * <p>
 * This service orchestrates the execution flow. It starts at the designated 'Start Node',
 * executes the node logic, resolves the next edge (static or conditional), and repeats
 * until a terminal state is reached or the maximum step limit is exceeded.
 * </p>
 * <p>
 * It ensures thread safety for the request scope but assumes {@link AgentState} is mutable
 * and not shared concurrently across different execution threads.
 * </p>
 */
@Log4j2
@Service
public class GraphExecutor {
    private static final int MAX_STEPS = 25; // Safety limit to prevent infinite loops

    private final GraphRegistry registry;

    public GraphExecutor(GraphRegistry registry) {
        this.registry = registry;
    }

    /**
     * The main entry point to run the graph.
     * @param initialState The starting data for the agent.
     * @return The final state after the graph completes.
     */
    public AgentState run(AgentState initialState) {
        String currentNodeName = registry.getStartNode();
        AgentState currentState = initialState;
        int stepCount = 0;

        if (currentNodeName == null) {
            throw new IllegalStateException("Graph cannot start: No node marked with isStart=true");
        }

        log.info(">>> Graph Execution Started. Start Node: [{}]", currentNodeName);

        // THE MAIN LOOP
        while (currentNodeName != null) {

            // 1. Safety Check for Infinite Loops
            if (stepCount >= MAX_STEPS) {
                log.error("!!! Graph terminated: Exceeded max steps ({}) !!!", MAX_STEPS);
                throw new RuntimeException("Graph execution exceeded maximum allowed steps. Check for infinite loops.");
            }

            // 2. Fetch the Node Bean
            NodeAction nodeAction = registry.getNode(currentNodeName);
            if (nodeAction == null) {
                log.error("Graph Error: Node '{}' was expected but not found in registry.", currentNodeName);
                break;
            }

            // 3. Execute the Node Logic
            log.info("--- Step {}: Executing [{}] ---", stepCount + 1, currentNodeName);
            try {
                currentState = nodeAction.execute(currentState);
            } catch (Exception e) {
                log.error("Error executing node [{}]: {}", currentNodeName, e.getMessage());
                throw e; // Or handle gracefully depending on policy
            }

            // 4. Determine Where to Go Next
            // We pass the *updated* state because conditional logic relies on the results of the execution
            String nextNodeName = registry.getNextNode(currentNodeName, currentState);

            if (nextNodeName != null) {
                log.info("    -> Transitioning to: [{}]", nextNodeName);
            } else {
                log.info("    -> End of Graph reached.");
            }

            // 5. Advance
            currentNodeName = nextNodeName;
            stepCount++;
        }

        log.info("<<< Graph Execution Completed in {} steps.", stepCount);
        return currentState;
    }
}