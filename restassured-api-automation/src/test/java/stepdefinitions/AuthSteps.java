package stepdefinitions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import clients.AuthService;
import config.ConfigReader;
import context.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

/** Steps covering the authentication feature. */
public class AuthSteps {

    private final ScenarioContext context;

    public AuthSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("I log in with the configured credentials")
    public void iLogInWithTheConfiguredCredentials() {
        ConfigReader config = ConfigReader.load();
        Response response = AuthService.login(
                config.getRequired("api.username"), config.getRequired("api.password"));
        context.setLastResponse(response);
    }

    @Then("the login succeeds and a token is returned")
    public void theLoginSucceedsAndATokenIsReturned() {
        Response response = context.getLastResponse();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.jsonPath().getString("token"), not(Matchers.blankOrNullString()));
    }
}
