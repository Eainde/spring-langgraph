package core;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic state container that persists across the lifecycle of a Graph execution.
 * <p>
 * This class acts as a shared context or "blackboard" where Nodes can read input arguments
 * and write their results. It essentially passes the baton of data from one node to the next.
 * </p>
 * <p>
 * Data is stored in a simple Key-Value map, making it flexible for any data type
 * (Strings, JSON objects, Lists, etc.).
 * </p>
 */
public class AgentState {
    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(data.get(key));
    }

    public Map<String, Object> getData() {
        return data;
    }
}