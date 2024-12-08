package dev.vudovenko.eventmanagement.locations.mappers;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocationEntityMapper implements EntityMapper<Location, LocationEntity> {

    private final EntityMapper<Event, EventEntity> eventEntityMapper;

    @Override
    public Location toDomain(LocationEntity locationEntity) {
        Set<Event> events =
                Hibernate.isInitialized(locationEntity.getEvents())
                        ? locationEntity.getEvents()
                        .stream()
                        .map(eventEntityMapper::toDomain)
                        .collect(Collectors.toSet())
                        : Collections.emptySet();
        return new Location(
                locationEntity.getId(),
                locationEntity.getName(),
                locationEntity.getAddress(),
                locationEntity.getCapacity(),
                locationEntity.getDescription(),
                events
        );
    }

    @Override
    public LocationEntity toEntity(Location location) {
        Set<EventEntity> events = location.getEvents() == null
                ? Collections.emptySet()
                : location.getEvents().stream()
                .map(eventEntityMapper::toEntity)
                .collect(Collectors.toSet());

        return new LocationEntity(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCapacity(),
                location.getDescription(),
                events
        );
    }
}
