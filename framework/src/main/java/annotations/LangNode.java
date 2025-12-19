package annotations;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Marks a Spring component as a generic Node in the SpringGraph workflow.
 * <p>
 * A Node represents a single unit of work or execution step in the AI agent lifecycle.
 * Classes annotated with this must implement the {@link com.springgraph.core.NodeAction} interface.
 * </p>
 *
 * @see com.springgraph.core.NodeAction
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // Automatically makes it a Spring Bean
public @interface LangNode {
    /**
     * The unique identifier for this node.
     * This name is used by Edges to target this node.
     * @return The unique node ID (e.g., "researcher", "writer").
     */
    String name();
    /**
     * Indicates if this node is the entry point of the graph.
     * Exactly one node in the application context must be marked as true.
     * @return true if this is the start node, false otherwise.
     */
    boolean isStart() default false;
    /**
     * Indicates if this node is a terminal point.
     * If true, the graph execution stops after this node finishes.
     * @return true if this is an end node, false otherwise.
     */
    boolean isEnd() default false;
}