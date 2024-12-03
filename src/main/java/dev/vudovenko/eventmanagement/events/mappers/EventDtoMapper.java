package dev.vudovenko.eventmanagement.events.mappers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import org.springframework.stereotype.Component;

@Component
public class EventDtoMapper implements DtoMapper<Event, EventDto> {

    @Override
    public Event toDomain(EventDto eventDto) {
        return new Event(
                eventDto.id(),
                eventDto.name(),
                eventDto.ownerId(),
                eventDto.maxPlaces(),
                eventDto.occupiedPlaces(),
                eventDto.date(),
                eventDto.cost(),
                eventDto.duration(),
                eventDto.locationId(),
                eventDto.status()
        );
    }

    @Override
    public EventDto toDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getOwnerId(),
                event.getMaxPlaces(),
                event.getOccupiedPlaces(),
                event.getDate(),
                event.getCost(),
                event.getDuration(),
                event.getLocationId(),
                event.getStatus()
        );
    }
}
