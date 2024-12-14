package dev.vudovenko.eventmanagement.events.services.validations;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.exceptions.*;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import dev.vudovenko.eventmanagement.security.authentication.AuthenticationService;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventValidationService {

    private final LocationService locationService;
    private final AuthenticationService authenticationService;

    public void checkCorrectnessDate(Event event) {
        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new DateEventInPastException(event.getDate());
        }
    }

    public void checkAvailabilityLocationPlaces(Event event) {
        Long locationId = event.getLocation().getId();
        if (!locationService.existsById(locationId)) {
            throw new LocationNotFoundException(locationId);
        }

        Integer availablePlaces = locationService
                .getNumberAvailableSeatsWithoutTakingIntoAccountEvent(
                        locationId,
                        event.getId()
                );

        if (event.getMaxPlaces() > availablePlaces) {
            throw new InsufficientSeatsException(availablePlaces);
        }
    }

    public void checkRightsToManageEvent(Event event) {
        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();
        if (currentUser.getRole().equals(UserRole.USER)
                && !event.getOwner().equals(currentUser)) {
            throw new UserNotEventCreatorException(currentUser.getId(), event.getId());
        }
    }

    public void checkIfEventHasAlreadyBeenCanceled(Event event) {
        if (event.getStatus().equals(EventStatus.CANCELLED)) {
            throw new EventAlreadyCancelledException(event.getName());
        }
    }

    public void checkIfEventHasStarted(Event event) {
        if (!event.getStatus().equals(EventStatus.WAIT_START)) {
            throw new CannotDeleteStartedEventException(event.getName(), event.getDate());
        }
    }

    public void checkThatOccupiedSeatsArePlacedInMaximumPlaces(Event event, Event notUpdatedEvent) {
        if (event.getMaxPlaces() < notUpdatedEvent.getOccupiedPlaces()) {
            throw new EventOccupiedPlacesExceedMaxPlacesException(
                    notUpdatedEvent.getName(),
                    notUpdatedEvent.getOccupiedPlaces(),
                    event.getMaxPlaces()
            );
        }
    }
}
