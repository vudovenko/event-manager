package dev.vudovenko.eventmanagement.locations.exceptions;

public class LocationNotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Location with id %d not found";

    public LocationNotFoundException(Long id) {
        super(MESSAGE_TEMPLATE.formatted(id));
    }
}
