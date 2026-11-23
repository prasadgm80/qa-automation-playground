package models.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response wrapper of {@code POST /events}, holding the created event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventResponse(boolean success, Event data, String message) {
}
