package config;

import engine.WorkflowEngine;
import observability.NodeObservabilityAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AgentFlowConfiguration {
    @Bean
    public WorkflowEngine workflowEngine() {
        return new WorkflowEngine();
    }

    @Bean
    public NodeObservabilityAspect nodeObservabilityAspect() {
        return new NodeObservabilityAspect();
    }
}
