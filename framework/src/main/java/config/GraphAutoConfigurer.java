package config;

import annotations.LangConditionalEdge;
import annotations.LangEdge;
import annotations.LangEdges;
import annotations.LangNode;
import core.ConditionEvaluator;
import core.NodeAction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Configuration
public class GraphAutoConfigurer {

    private final ApplicationContext context;
    private final GraphRegistry registry;

    public GraphAutoConfigurer(ApplicationContext context, GraphRegistry registry) {
        this.context = context;
        this.registry = registry;
    }

    @PostConstruct
    public void initGraph() {
        System.out.println("--- SpringGraph Framework: Initializing Graph ---");

        // Step 1: Find all beans annotated with @annotations.LangNode
        Map<String, Object> nodeBeans = context.getBeansWithAnnotation(LangNode.class);

        // A temporary set to track all registered node names for validation later
        Set<String> validNodeNames = new HashSet<>();

        // --- PHASE 1: REGISTER NODES ---
        nodeBeans.forEach((beanName, bean) -> {
            // specific check to ensure type safety
            if (!(bean instanceof NodeAction)) {
                throw new IllegalStateException("Bean '" + beanName + "' is annotated with @annotations.LangNode but does not implement core.NodeAction interface.");
            }

            LangNode nodeAnno = bean.getClass().getAnnotation(LangNode.class);
            String graphNodeName = nodeAnno.name();
            boolean isStart = nodeAnno.isStart();

            // Register the node in the engine
            registry.registerNode(graphNodeName, (NodeAction) bean, isStart);
            validNodeNames.add(graphNodeName);

            System.out.println("Registered Node: " + graphNodeName + (isStart ? " [START NODE]" : ""));
        });

        // --- PHASE 2: REGISTER EDGES ---
        nodeBeans.forEach((beanName, bean) -> {
            LangNode nodeAnno = bean.getClass().getAnnotation(LangNode.class);
            String sourceNodeName = nodeAnno.name();
            Class<?> beanClass = bean.getClass();

            // 1. Handle Single Static Edge (@annotations.LangEdge)
            if (beanClass.isAnnotationPresent(LangEdge.class)) {
                LangEdge edge = beanClass.getAnnotation(LangEdge.class);
                registry.registerStaticEdge(sourceNodeName, edge.target());
                System.out.println(" -> Edge: " + sourceNodeName + " -> " + edge.target());
            }

            // 2. Handle Multiple Static Edges (@annotations.LangEdges)
            if (beanClass.isAnnotationPresent(LangEdges.class)) {
                for (LangEdge edge : beanClass.getAnnotation(LangEdges.class).value()) {
                    registry.registerStaticEdge(sourceNodeName, edge.target());
                    System.out.println(" -> Edge: " + sourceNodeName + " -> " + edge.target());
                }
            }

            // 3. Handle Conditional Edges (@annotations.LangConditionalEdge)
            if (beanClass.isAnnotationPresent(LangConditionalEdge.class)) {
                LangConditionalEdge edge = beanClass.getAnnotation(LangConditionalEdge.class);
                Class<? extends ConditionEvaluator> deciderClass = edge.decider();

                ConditionEvaluator evaluatorInstance;

                // Try to get the Evaluator from Spring Context first (to support dependency injection)
                try {
                    evaluatorInstance = context.getBean(deciderClass);
                } catch (Exception e) {
                    // Fallback: Manually instantiate if not a bean
                    try {
                        evaluatorInstance = deciderClass.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException("Could not instantiate core.ConditionEvaluator: " + deciderClass.getName(), ex);
                    }
                }

                registry.registerConditionalEdge(sourceNodeName, evaluatorInstance);
                System.out.println(" -> Conditional Edge: " + sourceNodeName + " uses logic " + deciderClass.getSimpleName());
            }
        });

        // --- PHASE 3: VALIDATION ---
        validateGraph(validNodeNames);

        System.out.println("--- SpringGraph Framework: Initialization Complete ---");
    }

    /**
     * Checks if all edges point to valid nodes.
     */
    private void validateGraph(Set<String> validNodeNames) {
        // We need to ask the registry for all the targets it knows about
        // This requires adding a small helper method to engine.GraphRegistry to expose targets
        // For now, we will assume strict runtime checking, but ideally, you check here.

        if (registry.getStartNode() == null) {
            throw new IllegalStateException("Graph Error: No node is marked as 'isStart=true'. The graph must have an entry point.");
        }

        // You could iterate over registry.getAllEdges() here to ensure targets exist in validNodeNames
    }
}