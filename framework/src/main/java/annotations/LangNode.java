package annotations;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // Automatically makes it a Spring Bean
public @interface LangNode {
    String name(); // The unique ID of the node
    boolean isStart() default false; // Is this the entry point?
    boolean isEnd() default false; // Is this a terminal node?
}