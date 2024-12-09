package dev.vudovenko.eventmanagement.locations.services;

import dev.vudovenko.eventmanagement.locations.domain.Location;

import java.util.List;

public interface LocationService {

    List<Location> getAllLocations();

    Location getById(Long id);

    boolean existsById(Long locationId);

    Location createLocation(Location location);

    Location updateLocation(Long id, Location location);

    Location deleteLocation(Long id);

    Integer getNumberAvailableSeatsWithoutTakingIntoAccountEvent(Long locationId, Long eventId);
}
