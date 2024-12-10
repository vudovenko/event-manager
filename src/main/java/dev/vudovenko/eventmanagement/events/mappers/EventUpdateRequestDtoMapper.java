package dev.vudovenko.eventmanagement.events.mappers;

import dev.vudovenko.eventmanagement.common.mappers.ToDomainMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventUpdateRequestDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class EventUpdateRequestDtoMapper implements ToDomainMapper<Event, EventUpdateRequestDto> {

    @Override
    public Event toDomain(EventUpdateRequestDto eventCreateRequestDto) {
        Location location = new Location();
        location.setId(eventCreateRequestDto.locationId());
        location.setEvents(Collections.emptySet());

        return new Event(
                null,
                eventCreateRequestDto.name(),
                null,
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
