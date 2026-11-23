package stepdefinitions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import auth.TokenManager;
import clients.EventService;
import context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.Map;
import models.event.CreateEventRequest;
import models.event.EventListResponse;
import models.event.EventResponse;
import org.hamcrest.Matchers;
import utils.DateUtils;

/** Steps covering the events feature. */
public class EventSteps {

    private static final String TOKEN_KEY = "token";
    private static final String REQUEST_KEY = "createEventRequest";

    private final ScenarioContext context;

    public EventSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("I am authenticated")
    public void iAmAuthenticated() {
        context.set(TOKEN_KEY, TokenManager.getBearerToken());
    }

    @When("I create a new event with the following details")
    public void iCreateANewEventWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        CreateEventRequest request = new CreateEventRequest(
                data.get("title"),
                data.get("description"),
                data.get("category"),
                data.get("venue"),
                data.get("city"),
                DateUtils.futureDate(Integer.parseInt(data.get("daysFromNow"))),
                Double.parseDouble(data.get("price")),
                Integer.parseInt(data.get("totalSeats")),
                data.get("imageUrl"));

        context.set(REQUEST_KEY, request);
        context.setLastResponse(EventService.createEvent(context.get(TOKEN_KEY), request));
    }

    @Then("the event is created successfully")
    public void theEventIsCreatedSuccessfully() {
        Response response = context.getLastResponse();
        assertThat(response.statusCode(), equalTo(201));

        EventResponse body = response.as(EventResponse.class);
        CreateEventRequest sent = context.get(REQUEST_KEY);
        assertThat(body.success(), is(true));
        assertThat(body.data().id(), greaterThan(0L));
        assertThat(body.data().title(), equalTo(sent.title()));
    }

    @When("I request the list of events")
    public void iRequestTheListOfEvents() {
        context.setLastResponse(EventService.getEvents(context.get(TOKEN_KEY), 10));
    }

    @Then("the events are returned successfully")
    public void theEventsAreReturnedSuccessfully() {
        Response response = context.getLastResponse();
        assertThat(response.statusCode(), equalTo(200));

        EventListResponse body = response.as(EventListResponse.class);
        assertThat(body.success(), is(true));
        assertThat(body.data(), is(not(Matchers.empty())));
    }
}
