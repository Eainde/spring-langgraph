package annotations;

import core.ConditionEvaluator;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LangConditionalEdge {
    Class<? extends ConditionEvaluator> decider(); // The class that decides where to go
}