package annotations;

import core.ConditionEvaluator;

import java.lang.annotation.*;

/**
 * Defines a dynamic, conditional transition logic for the annotated Node.
 * <p>
 * Unlike a static {@link LangEdge}, a conditional edge does not point to a specific node.
 * Instead, it points to a logic class (ConditionEvaluator) that will analyze the
 * runtime state and decide which node should be executed next.
 * </p>
 * <p>
 * Use this for routing logic, such as "If data is found -> Writer, else -> Stop".
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LangConditionalEdge {
    /**
     * The class responsible for evaluating the state and determining the next node.
     * This class must implement {@link com.springgraph.core.ConditionEvaluator}.
     * @return The evaluator class.
     */
    Class<? extends ConditionEvaluator> decider();
}