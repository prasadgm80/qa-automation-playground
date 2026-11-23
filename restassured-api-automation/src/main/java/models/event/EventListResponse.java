package models.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Response wrapper of {@code GET /events}, holding the page of events. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventListResponse(boolean success, List<Event> data, Pagination pagination) {

    /** Paging metadata returned with an event list. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(int total, int page, int limit, int totalPages) {
    }
}
