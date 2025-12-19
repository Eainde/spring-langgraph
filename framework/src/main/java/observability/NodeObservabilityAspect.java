package observability;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import state.AgentState;

@Log4j2
@Component
public class NodeObservabilityAspect {

    @org.aspectj.lang.annotation.Around("execution(* com.financial.agentflow.core.AgentNode.execute(..))")
    public Object measureNode(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String nodeName = joinPoint.getTarget().getClass().getSimpleName();
        AgentState state = (AgentState) joinPoint.getArgs()[0];

        //log.info("[AUDIT] Node Start: {} | ID: {}", nodeName, state.getCorrelationId());

        try {
            Object result = joinPoint.proceed();
            //log.info("[AUDIT] Node Success: {} | Duration: {}ms", nodeName, (System.currentTimeMillis() - start));
            return result;
        } catch (Exception e) {
            //log.error("[AUDIT] Node Failure: {} | Error: {}", nodeName, e.getMessage());
            throw e;
        }
    }
}
