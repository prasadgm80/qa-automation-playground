package stepdefinitions;

import context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs scenario boundaries and attaches the last API response to the report on failure. */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

    private final ScenarioContext context;

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        LOG.info("Starting scenario: {}", scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        LOG.info("Finished scenario: {} -> {}", scenario.getName(), scenario.getStatus());

        Response lastResponse = context.getLastResponse();
        if (scenario.isFailed() && lastResponse != null) {
            scenario.attach(
                    lastResponse.asString().getBytes(StandardCharsets.UTF_8),
                    "application/json",
                    "Last API response");
        }
    }
}
