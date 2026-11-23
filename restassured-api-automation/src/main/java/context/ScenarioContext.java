package context;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-scenario state shared between step classes (injected by picocontainer),
 * so parallel scenarios never interfere with each other.
 */
public class ScenarioContext {

    private final Map<String, Object> store = new HashMap<>();
    private Response lastResponse;

    /** Stores an arbitrary value under {@code key} for later steps. */
    public void set(String key, Object value) {
        store.put(key, value);
    }

    /** Returns the value previously stored under {@code key}, cast to {@code T}. */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) store.get(key);
    }

    /** Remembers the most recent API response. */
    public void setLastResponse(Response response) {
        this.lastResponse = response;
    }

    /** Returns the most recent API response. */
    public Response getLastResponse() {
        return lastResponse;
    }
}
