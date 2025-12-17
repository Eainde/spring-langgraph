package com.eainde.agent;


import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

// The shared memory of our graph
public class SimpleAgentState extends AgentState {

    public SimpleAgentState(Map<String, Object> initData) {
        super(initData);
    }

    public String getInput() {
        // use value() which safely retrieves data as an Optional
        return this.<String>value("input").orElse("");
    }

    public String getLastResponse() {
        return this.<String>value("response").orElse(null);
    }
}
