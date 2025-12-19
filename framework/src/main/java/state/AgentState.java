package state;

import java.util.HashMap;
import java.util.Map;

public abstract class AgentState extends org.bsc.langgraph4j.state.AgentState {
    // We add our enterprise headers here
    private String workflowId;
    private String workflowVersion;
    private String correlationId;
    private Map<String, Object> metadata = new HashMap<>();

    // Constructor matching super if required by your specific library version
    public AgentState(Map<String, Object> initData) {
        super(initData);
    }

    public AgentState() {
        super(new HashMap<>());
    }

    // Standard getters/setters for our custom fields
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getWorkflowVersion() { return workflowVersion; }
    public void setWorkflowVersion(String workflowVersion) { this.workflowVersion = workflowVersion; }
}
