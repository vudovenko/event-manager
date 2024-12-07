package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class LocationTestUtils {

    @Autowired
    private LocationService locationService;
    @Autowired
    private EntityMapper<Location, LocationEntity> locationEntityMapper;

    public Location getCreatedLocation() {
        return locationService.createLocation(
                new Location(
                        null,
                        "location-" + RandomUtils.getRandomInt(),
                        "address-" + RandomUtils.getRandomInt(),
                        100,
                        "description",
                        new HashSet<>()
                )
        );
    }

    public LocationEntity getCreatedLocationEntity() {
        return locationEntityMapper.toEntity(getCreatedLocation());
    }

    public LocationDto getWrongLocationDto() {
        return new LocationDto(
                Long.MAX_VALUE,
                null,
                "   ",
                1,
                null
        );
    }
}
