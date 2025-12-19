package annotations;

import java.lang.annotation.*;

/**
 * Defines a static, unconditional transition from the annotated Node to another Node.
 * <p>
 * This annotation is placed on a class annotated with {@link LangNode}.
 * It instructs the engine to strictly transition to the target node
 * upon successful execution of the current node.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(LangEdges.class)
public @interface LangEdge {
    /**
     * The name of the target node to transition to.
     * This must match the {@link LangNode#name()} of a registered bean.
     * @return The target node ID.
     */
    String target(); // The name of the next node
    String condition() default ""; // Optional: SPEL expression or method name for conditional logic
}