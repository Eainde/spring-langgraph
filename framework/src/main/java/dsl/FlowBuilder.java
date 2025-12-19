package dsl;

import node.AgentNode;
import node.AgentRouter;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.ApplicationContext;
import state.AgentState;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;

/**
 * 2. FLUENT BUILDER API
 * Removes the boilerplate of manually instantiating StateGraph and wiring strings.
 */
public class FlowBuilder<S extends AgentState> {

    // Using the REAL StateGraph from the library
    private final StateGraph<S> graph;
    private final ApplicationContext context;
    private String lastNodeName;


    public FlowBuilder(Class<S> stateClass, ApplicationContext context) {
        this.context = context;
        try {
            // FIX: Use Reflection to instantiate StateGraph.
            // This bypasses compile-time "Cannot resolve constructor" errors caused by
            // strict generic bounds or minor library version differences.
            this.graph = instantiateGraph(stateClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize StateGraph. Check LangGraph4j version compatibility.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private StateGraph<S> instantiateGraph(Class<S> stateClass) throws Exception {
        // Strategy 1: Try constructor that takes a Class (Most common)
        try {
            Constructor<?> ctor = StateGraph.class.getConstructor(Class.class);
            return (StateGraph<S>) ctor.newInstance(stateClass);
        } catch (NoSuchMethodException e1) {
            // Strategy 2: Try No-Arg constructor (Fallback)
            try {
                Constructor<?> ctor = StateGraph.class.getConstructor();
                return (StateGraph<S>) ctor.newInstance();
            } catch (NoSuchMethodException e2) {
                // Strategy 3: Try generic Object constructor (Rare)
                throw new RuntimeException("Could not find a valid constructor for StateGraph(Class) or StateGraph()", e1);
            }
        }
    }


    // Add a node by Class (Spring Bean lookup)
    public FlowBuilder<S> startWith(Class<? extends AgentNode<S>> nodeClass) {
        String name = registerNode(nodeClass);
        try {
            // FIX: Use START constant instead of setEntryPoint
            graph.addEdge(START, name);
        } catch (Exception e) {
            throw new RuntimeException("Error setting entry point", e);
        }
        this.lastNodeName = name;
        return this;
    }

    public FlowBuilder<S> next(Class<? extends AgentNode<S>> nodeClass) {
        String name = registerNode(nodeClass);
        if (lastNodeName != null) {
            try {
                graph.addEdge(lastNodeName, name);
            } catch (Exception e) {
                throw new RuntimeException("Error adding edge", e);
            }
        }
        this.lastNodeName = name;
        return this;
    }

    // Conditional Routing
    public RoutingStage choice(Class<? extends AgentRouter<S>> routerClass) {
        AgentRouter<S> routerBean = context.getBean(routerClass);
        return new RoutingStage(routerBean, lastNodeName);
    }

    // Parallel Execution
    public ParallelStage parallel() {
        return new ParallelStage(lastNodeName);
    }

    // Internal helper to register node bean into the graph
    private String registerNode(Class<? extends AgentNode<S>> nodeClass) {
        String name = nodeClass.getSimpleName();
        AgentNode<S> bean = context.getBean(nodeClass);

        // FIX: Wrap the bean execution to adapt to LangGraph's expected signature.
        // LangGraph expects a return of CompletableFuture<Map<String, Object>> (Partial State Update).
        try {
            graph.addNode(name, (state) -> {
                // 1. Execute the synchronous AgentNode
                S resultState = bean.execute(state);
                // 2. Return the data map wrapped in a CompletableFuture
                return CompletableFuture.completedFuture(resultState.data());
            });
        } catch (Exception e) {
            throw new RuntimeException("Error adding node to graph: " + name, e);
        }
        return name;
    }

    public CompiledGraph<S> build() {
        if (lastNodeName != null) {
            try {
                // FIX: Use END constant instead of setFinishPoint
                graph.addEdge(lastNodeName, END);
            } catch (Exception e) {
                throw new RuntimeException("Error setting finish point", e);
            }
        }
        try {
            return graph.compile();
        } catch (Exception e) {
            throw new RuntimeException("Error compiling graph", e);
        }
    }

    // Inner class for fluent routing configuration
    public class RoutingStage {
        private final AgentRouter<S> router;
        private final String sourceNode;
        private final Map<String, String> routes = new HashMap<>();

        public RoutingStage(AgentRouter<S> router, String sourceNode) {
            this.router = router;
            this.sourceNode = sourceNode;
        }

        public RoutingStage when(String condition, Class<? extends AgentNode<S>> targetClass) {
            String targetName = registerNode(targetClass);
            routes.put(condition, targetName);
            return this;
        }

        public FlowBuilder<S> endRouting() {
            try {
                // Adapting our AgentRouter to LangGraph's conditional edge signature
                // FIX: Wrap the router result in a CompletableFuture
                graph.addConditionalEdges(
                        sourceNode,
                        (state) -> CompletableFuture.completedFuture(router.route(state)),
                        routes
                );
            } catch (Exception e) {
                throw new RuntimeException("Error adding conditional edges", e);
            }

            FlowBuilder.this.lastNodeName = null;
            return FlowBuilder.this;
        }
    }

    // Inner class for Parallel configuration
    public class ParallelStage {
        private final String sourceNode;
        private final List<String> branchEndNodes = new ArrayList<>();

        public ParallelStage(String sourceNode) {
            this.sourceNode = sourceNode;
        }

        public ParallelStage add(Class<? extends AgentNode<S>> nodeClass) {
            String name = registerNode(nodeClass);
            if (sourceNode != null) {
                try {
                    graph.addEdge(sourceNode, name);
                } catch (Exception e) {
                    throw new RuntimeException("Error adding parallel edge", e);
                }
            }
            branchEndNodes.add(name);
            return this;
        }

        public FlowBuilder<S> join(Class<? extends AgentNode<S>> joinNodeClass) {
            String joinName = registerNode(joinNodeClass);
            for (String branchEnd : branchEndNodes) {
                try {
                    graph.addEdge(branchEnd, joinName);
                } catch (Exception e) {
                    throw new RuntimeException("Error adding join edge", e);
                }
            }
            FlowBuilder.this.lastNodeName = joinName;
            return FlowBuilder.this;
        }
    }
}
