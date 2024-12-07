package dev.vudovenko.eventmanagement.events.mappers;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventEntityMapper implements EntityMapper<Event, EventEntity> {

    private final EntityMapper<User, UserEntity> userEntityMapper;
    private final EntityMapper<Location, LocationEntity> locationEntityMapper;

    @Override
    public EventEntity toEntity(Event event) {
        return new EventEntity(
                event.getId(),
                event.getName(),
                userEntityMapper.toEntity(event.getOwner()),
                event.getMaxPlaces(),
                event.getOccupiedPlaces(),
                event.getDate(),
                event.getCost(),
                event.getDuration(),
                locationEntityMapper.toEntity(event.getLocation()),
                event.getStatus()
        );
    }

    @Override
    public Event toDomain(EventEntity eventEntity) {
        return new Event(
                eventEntity.getId(),
                eventEntity.getName(),
                userEntityMapper.toDomain(eventEntity.getOwner()),
                eventEntity.getMaxPlaces(),
                eventEntity.getOccupiedPlaces(),
                eventEntity.getDate(),
                eventEntity.getCost(),
                eventEntity.getDuration(),
                locationEntityMapper.toDomain(eventEntity.getLocation()),
                eventEntity.getStatus()
        );
    }
}
