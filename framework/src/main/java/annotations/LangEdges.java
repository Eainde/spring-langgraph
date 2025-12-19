package annotations;

import java.lang.annotation.*;

/**
 * Container annotation for {@link LangEdge}.
 * <p>
 * This annotation is rarely used directly by the developer. It exists to support
 * the usage of multiple {@code @LangEdge} annotations on a single class
 * (Java's Repeatable annotation mechanism).
 * </p>
 * <p>
 * Example usage (implicit):
 * <pre>
 * {@code
 * @LangNode(name = "trigger")
 * @LangEdge(target = "serviceA")
 * @LangEdge(target = "serviceB") // This requires LangEdges to exist behind the scenes
 * public class TriggerNode ...
 * }
 * </pre>
 * </p>
 *
 * @see LangEdge
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LangEdges {
    /**
     * The array of edge definitions.
     * @return An array of LangEdge annotations.
     */
    LangEdge[] value();
}