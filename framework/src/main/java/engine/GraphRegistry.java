package engine;

import core.AgentState;
import core.ConditionEvaluator;
import core.NodeAction;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * The internal registry that holds the structure of the compiled graph.
 * <p>
 * This class acts as the "routing table" for the framework. It stores mappings of:
 * <ul>
 * <li>Node Names -> Node Action Instances</li>
 * <li>Source Nodes -> Target Nodes (Static Edges)</li>
 * <li>Source Nodes -> Condition Evaluators (Dynamic Edges)</li>
 * </ul>
 * </p>
 * <p>
 * It is populated at startup by {@link com.springgraph.config.GraphAutoConfigurer} and
 * queried at runtime by {@link GraphExecutor}.
 * </p>
 */
@Service
public class GraphRegistry {
    private final Map<String, NodeAction> nodes = new HashMap<>();

    // Static Edges: NodeName -> NextNodeName
    private final Map<String, String> staticEdges = new HashMap<>();

    // Conditional Edges: NodeName -> EvaluatorInstance
    private final Map<String, ConditionEvaluator> conditionalEdges = new HashMap<>();

    private String startNode;

    public void registerNode(String name, NodeAction action, boolean isStart) {
        nodes.put(name, action);
        if (isStart) this.startNode = name;
    }

    public void registerStaticEdge(String source, String target) {
        staticEdges.put(source, target);
    }

    public void registerConditionalEdge(String source, ConditionEvaluator evaluator) {
        conditionalEdges.put(source, evaluator);
    }

    public NodeAction getNode(String name) {
        return nodes.get(name);
    }

    public String getStartNode() {
        return startNode;
    }

    // UPDATED LOGIC: Check conditional first, then static
    public String getNextNode(String current, AgentState state) {
        if (conditionalEdges.containsKey(current)) {
            // Run the logic to decide the path
            return conditionalEdges.get(current).determineNextNode(state);
        }
        return staticEdges.get(current);
    }
}