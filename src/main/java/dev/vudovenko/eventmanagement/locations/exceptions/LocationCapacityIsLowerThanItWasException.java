package dev.vudovenko.eventmanagement.locations.exceptions;

public class LocationCapacityIsLowerThanItWasException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "The location's capacity was = %d, but now = %d";

    public LocationCapacityIsLowerThanItWasException(Integer previousCapacity, Integer newCapacity) {
        super(MESSAGE_TEMPLATE.formatted(previousCapacity, newCapacity));
    }
}
