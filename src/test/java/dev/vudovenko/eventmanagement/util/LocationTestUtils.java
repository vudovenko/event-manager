package dev.vudovenko.eventmanagement.util;

import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class LocationTestUtils {

    @Autowired
    private LocationService locationService;

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
