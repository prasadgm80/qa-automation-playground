package utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Computes event dates relative to "now" so test data never goes stale. */
public final class DateUtils {

    private DateUtils() {
    }

    /** Returns an ISO-8601 UTC date-time {@code daysFromNow} days in the future. */
    public static String futureDate(int daysFromNow) {
        return OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(daysFromNow)
                .withNano(0)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
