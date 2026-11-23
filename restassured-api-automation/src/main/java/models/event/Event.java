package models.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single event as returned by the API ({@code price} comes back as a string). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Event(
        long id,
        String title,
        String description,
        String category,
        String venue,
        String city,
        String eventDate,
        String price,
        int totalSeats,
        int availableSeats,
        String imageUrl) {
}
