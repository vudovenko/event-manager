package dev.vudovenko.eventmanagement.events.mappers;

import dev.vudovenko.eventmanagement.common.mappers.ToDomainMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.security.jwt.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCreateRequestDtoMapper implements ToDomainMapper<Event, EventCreateRequestDto> {

    private final JwtAuthenticationService jwtAuthenticationService;

    @Override
    public Event toDomain(EventCreateRequestDto eventCreateRequestDto) {
        Location location = new Location();
        location.setId(eventCreateRequestDto.locationId());

        return new Event(
                null,
                eventCreateRequestDto.name(),
                jwtAuthenticationService.getCurrentAuthenticatedUserOrThrow(),
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
