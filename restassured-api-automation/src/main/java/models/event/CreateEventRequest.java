package models.event;

/** Request body for {@code POST /events}. */
public record CreateEventRequest(
        String title,
        String description,
        String category,
        String venue,
        String city,
        String eventDate,
        double price,
        int totalSeats,
        String imageUrl) {
}
