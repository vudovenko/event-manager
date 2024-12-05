package dev.vudovenko.eventmanagement.events.exceptions;

public class InsufficientSeatsException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Only %d seats are available for the event";

    public InsufficientSeatsException(Integer availableSeats) {
        super(MESSAGE_TEMPLATE.formatted(availableSeats));
    }
}
