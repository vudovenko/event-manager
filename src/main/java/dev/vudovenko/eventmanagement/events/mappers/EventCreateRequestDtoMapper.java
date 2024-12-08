package dev.vudovenko.eventmanagement.events.mappers;

import dev.vudovenko.eventmanagement.common.mappers.ToDomainMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.security.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class EventCreateRequestDtoMapper implements ToDomainMapper<Event, EventCreateRequestDto> {

    private final AuthenticationService authenticationService;

    @Override
    public Event toDomain(EventCreateRequestDto eventCreateRequestDto) {
        Location location = new Location();
        location.setId(eventCreateRequestDto.locationId());
        location.setEvents(Collections.emptySet());

        return new Event(
                null,
                eventCreateRequestDto.name(),
                authenticationService.getCurrentAuthenticatedUserOrThrow(),
                eventCreateRequestDto.maxPlaces(),
                0,
                eventCreateRequestDto.date(),
                eventCreateRequestDto.cost(),
                eventCreateRequestDto.duration(),
                location,
                EventStatus.WAIT_START
        );
    }
}
