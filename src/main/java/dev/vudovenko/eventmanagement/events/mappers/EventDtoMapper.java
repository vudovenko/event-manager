package dev.vudovenko.eventmanagement.events.mappers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import dev.vudovenko.eventmanagement.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventDtoMapper implements DtoMapper<Event, EventDto> {

    private final UserService userService;
    private final LocationService locationService;

    @Override
    public Event toDomain(EventDto eventDto) {
        return new Event(
                eventDto.id(),
                eventDto.name(),
                userService.findById(eventDto.ownerId()),
                eventDto.maxPlaces(),
                eventDto.occupiedPlaces(),
                eventDto.date(),
                eventDto.cost(),
                eventDto.duration(),
                locationService.getById(eventDto.locationId()),
                eventDto.status()
        );
    }

    @Override
    public EventDto toDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getOwner().getId(),
                event.getMaxPlaces(),
                event.getOccupiedPlaces(),
                event.getDate(),
                event.getCost(),
                event.getDuration(),
                event.getLocation().getId(),
                event.getStatus()
        );
    }
}
