package clients;

import io.restassured.response.Response;
import models.event.CreateEventRequest;

/** Thin wrapper over the EventHub events endpoints. */
public final class EventService {

    private static final String EVENTS_PATH = "/events";

    private EventService() {
    }

    /** Creates an event (requires a bearer token) and returns the raw response. */
    public static Response createEvent(String bearerToken, CreateEventRequest event) {
        return RequestFactory.authorizedRequest(bearerToken)
                .body(event)
                .post(EVENTS_PATH);
    }

    /** Fetches a page of events (requires a bearer token) and returns the raw response. */
    public static Response getEvents(String bearerToken, int limit) {
        return RequestFactory.authorizedRequest(bearerToken)
                .queryParam("limit", limit)
                .get(EVENTS_PATH);
    }
}
