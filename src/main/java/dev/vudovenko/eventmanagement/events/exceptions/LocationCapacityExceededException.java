package dev.vudovenko.eventmanagement.events.exceptions;

public class LocationCapacityExceededException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE
            = "Location %s has %d empty seats and cannot accommodate event %s with maxPlaces = %d";

    public LocationCapacityExceededException(
            String locationName,
            int emptySeats,
            String eventName,
            int maxPlaces
    ) {
        super(
                MESSAGE_TEMPLATE.formatted(
                        locationName,
                        emptySeats,
                        eventName,
                        maxPlaces
                )
        );
    }
}
