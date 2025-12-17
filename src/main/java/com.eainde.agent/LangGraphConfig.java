package com.eainde.agent;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction; // UPDATED IMPORT
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Configuration
public class LangGraphConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are a helpful assistant. If the user asks about the weather, strictly reply with 'CALL_TOOL: WEATHER'.")
                .build();
    }

    @Bean
    public CompiledGraph<SimpleAgentState> agentGraph(ChatClient chatClient) throws GraphStateException {
        StateGraph<SimpleAgentState> workflow = new StateGraph<>(SimpleAgentState::new);

        // --- NODE 1: AGENT ---
        workflow.addNode("agent", state -> {
            String input = state.getInput();
            String response = chatClient.prompt().user(input).call().content();
            System.out.println(response);
            // Must return CompletableFuture for Async Node
            return CompletableFuture.completedFuture(Map.of("response", (Object) response));
        });

        // --- NODE 2: TOOLS ---
        workflow.addNode("tools", state -> {
            System.out.println("--- Executing Weather Tool ---");
            return CompletableFuture.completedFuture(
                    Map.of("response", (Object) "The weather is currently Sunny and 25°C.")
            );
        });

        // --- EDGE LOGIC (ASYNC) ---
        // The library now requires AsyncEdgeAction which returns CompletableFuture<String>
        AsyncEdgeAction<SimpleAgentState> conditionalEdge = state -> {
            String lastResponse = state.getLastResponse();
            if (lastResponse != null && lastResponse.contains("CALL_TOOL")) {
                return CompletableFuture.completedFuture("tools");
            }
            return CompletableFuture.completedFuture(END);
        };

        // --- WIRING ---
        workflow.addEdge(START, "agent");

        // Define route map to help Java generics
        Map<String, String> routeMap = Map.of(
                "tools", "tools",
                END, END
        );

        workflow.addConditionalEdges(
                "agent",
                conditionalEdge,
                routeMap
        );

        workflow.addEdge("tools", END);

        return workflow.compile();
    }
}