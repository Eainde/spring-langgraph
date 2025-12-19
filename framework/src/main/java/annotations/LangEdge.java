package annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(LangEdges.class)
public @interface LangEdge {
    String target(); // The name of the next node
    String condition() default ""; // Optional: SPEL expression or method name for conditional logic
}