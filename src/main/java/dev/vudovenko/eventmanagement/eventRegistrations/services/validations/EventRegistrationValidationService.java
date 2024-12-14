package dev.vudovenko.eventmanagement.eventRegistrations.services.validations;

import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.AlreadyRegisteredForEventException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventRegistrationNotFoundException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationCancellationException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationException;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class EventRegistrationValidationService {

    private final EventRegistrationService eventRegistrationService;

    public EventRegistrationValidationService(
            @Lazy EventRegistrationService eventRegistrationService
    ) {
        this.eventRegistrationService = eventRegistrationService;
    }

    public void checkReRegistration(Long eventId, User user) {
        if (eventRegistrationService.isUserRegisteredForEvent(user.getId(), eventId)) {
            throw new AlreadyRegisteredForEventException(eventId);
        }
    }

    public void checkThatEventStatusAllowsRegistration(Event event) {
        if (event.getStatus().equals(EventStatus.CANCELLED)
                || event.getStatus().equals(EventStatus.FINISHED)) {
            throw new EventStatusNotAllowedForRegistrationException(event.getId(), event.getStatus());
        }
    }

    public void checkAvailabilityPlace(Event event) {
        int availablePlaces = event.getMaxPlaces() - (event.getOccupiedPlaces() + 1);

        if (availablePlaces <= 0) {
            throw new InsufficientSeatsException(0);
        }
    }

    public void checkIfThereIsRegistrationForEvent(Long eventId, User user) {
        if (!eventRegistrationService.isUserRegisteredForEvent(user.getId(), eventId)) {
            throw new EventRegistrationNotFoundException(user.getId(), eventId);
        }
    }

    public void checkThatEventStatusAllowsCancelRegistration(Event event) {
        if (event.getStatus().equals(EventStatus.STARTED)
                || event.getStatus().equals(EventStatus.FINISHED)) {
            throw new EventStatusNotAllowedForRegistrationCancellationException(
                    event.getId(),
                    event.getStatus()
            );
        }
    }
}
