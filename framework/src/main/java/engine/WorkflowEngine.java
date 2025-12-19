package engine;

import dsl.FlowBuilder;
import dsl.WorkflowDefinition;
import lombok.extern.log4j.Log4j2;
import org.bsc.langgraph4j.CompiledGraph;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import state.AgentState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 3. RUNTIME ENGINE & REGISTRY
 * Manages the compilation and execution of workflows.
 */
@Log4j2
@Component
public class WorkflowEngine implements ApplicationContextAware {
    private ApplicationContext context;
    private final Map<String, RegisteredWorkflow> registry = new ConcurrentHashMap<>();

    // Wrapper to keep track of version metadata along with the graph
    private record RegisteredWorkflow(CompiledGraph<?> graph, String version) {}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    // Called on Startup via @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerWorkflows() {
        Map<String, WorkflowDefinition> beans = context.getBeansOfType(WorkflowDefinition.class);

        beans.forEach((beanName, def) -> {
            String wfId = def.getWorkflowId();
            String version = def.getVersion();

            //log.info("Compiling Workflow: {} (Version: {})", wfId, version);

            FlowBuilder builder = new FlowBuilder(def.getStateType(), context);
            def.define(builder);

            // Register using the returned ID, not the bean name
            registry.put(wfId, new RegisteredWorkflow(builder.build(), version));
        });
    }

    @SuppressWarnings("unchecked")
    public <S extends AgentState> S run(String workflowId, S initialState) {
        RegisteredWorkflow workflow = registry.get(workflowId);
        if (workflow == null) throw new IllegalArgumentException("Unknown workflow ID: " + workflowId);

        CompiledGraph<S> graph = (CompiledGraph<S>) workflow.graph();

        // Enrich state with metadata
        initialState.setWorkflowId(workflowId);
        initialState.setWorkflowVersion(workflow.version());

        // observability setup
        MDC.put("flow_id", workflowId);
        MDC.put("flow_version", workflow.version());
        MDC.put("correlation_id", initialState.getCorrelationId());

        try {
            //log.info("Starting Workflow Execution");
            java.util.stream.Stream<Map<String, Object>> stream = (Stream<Map<String, Object>>) graph.stream(initialState.data());
            Map<String, Object> finalResult = null;

            for (java.util.Iterator<Map<String, Object>> it = stream.iterator(); it.hasNext(); ) {
                finalResult = it.next();
            }

            // Update the typed state object with the final data map
            if (finalResult != null) {
                initialState.data().putAll(finalResult);
            }

            return initialState;
        } catch (Exception e) {
            //log.error("Error executing workflow", e);
            throw e;
        } finally {
            //log.info("Finished Workflow Execution");
            MDC.clear();
        }
    }
}
