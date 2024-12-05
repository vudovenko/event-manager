package dev.vudovenko.eventmanagement.locations.mappers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class LocationDtoMapper implements DtoMapper<Location, LocationDto> {

    @Override
    public Location toDomain(LocationDto locationDto) {
        return new Location(
                locationDto.id(),
                locationDto.name(),
                locationDto.address(),
                locationDto.capacity(),
                locationDto.description(),
                new HashSet<>()
        );
    }

    @Override
    public LocationDto toDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCapacity(),
                location.getDescription()
        );
    }
}
