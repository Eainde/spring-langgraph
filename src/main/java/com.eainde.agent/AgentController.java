package com.eainde.agent;

import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    // Inject CompiledGraph instead of Graph
    private final CompiledGraph<SimpleAgentState> agentGraph;

    public AgentController(CompiledGraph<SimpleAgentState> agentGraph) {
        this.agentGraph = agentGraph;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String userMessage) {
        try {
            Map<String, Object> inputs = Map.of("input", userMessage);

            System.out.println("--- Starting Graph Execution ---");

            // Invoke the graph
            Optional<SimpleAgentState> result = agentGraph.invoke(inputs);

            return result.map(SimpleAgentState::getLastResponse)
                    .orElse("Error: No response generated.");

        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing graph: " + e.getMessage();
        }
    }
}
