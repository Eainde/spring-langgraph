package engine;

import core.AgentState;
import core.NodeAction;
import org.springframework.stereotype.Service;

@Service
public class GraphExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GraphExecutor.class);
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

        logger.info(">>> Graph Execution Started. Start Node: [{}]", currentNodeName);

        // THE MAIN LOOP
        while (currentNodeName != null) {

            // 1. Safety Check for Infinite Loops
            if (stepCount >= MAX_STEPS) {
                logger.error("!!! Graph terminated: Exceeded max steps ({}) !!!", MAX_STEPS);
                throw new RuntimeException("Graph execution exceeded maximum allowed steps. Check for infinite loops.");
            }

            // 2. Fetch the Node Bean
            NodeAction nodeAction = registry.getNode(currentNodeName);
            if (nodeAction == null) {
                logger.error("Graph Error: Node '{}' was expected but not found in registry.", currentNodeName);
                break;
            }

            // 3. Execute the Node Logic
            logger.info("--- Step {}: Executing [{}] ---", stepCount + 1, currentNodeName);
            try {
                currentState = nodeAction.execute(currentState);
            } catch (Exception e) {
                logger.error("Error executing node [{}]: {}", currentNodeName, e.getMessage());
                throw e; // Or handle gracefully depending on policy
            }

            // 4. Determine Where to Go Next
            // We pass the *updated* state because conditional logic relies on the results of the execution
            String nextNodeName = registry.getNextNode(currentNodeName, currentState);

            if (nextNodeName != null) {
                logger.info("    -> Transitioning to: [{}]", nextNodeName);
            } else {
                logger.info("    -> End of Graph reached.");
            }

            // 5. Advance
            currentNodeName = nextNodeName;
            stepCount++;
        }

        logger.info("<<< Graph Execution Completed in {} steps.", stepCount);
        return currentState;
    }
}