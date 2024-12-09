package dev.vudovenko.eventmanagement.events.exceptions;

public class EventOccupiedPlacesExceedMaxPlacesException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE
            = "In event \"%s\", the number of occupied places (%d) exceeds the new maximum capacity (%d)";

    public EventOccupiedPlacesExceedMaxPlacesException(
            String eventName, Integer occupiedPlaces, Integer newMaxPlaces) {
        super(
                MESSAGE_TEMPLATE.formatted(
                        eventName,
                        occupiedPlaces,
                        newMaxPlaces
                )
        );
    }
}
