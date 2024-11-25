package dev.vudovenko.eventmanagement.locations.services;

import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.services.impl.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {


    @Override
    public List<Location> getAllLocations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getById(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location createLocation(Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location updateLocation(Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteLocation(Long id) {
        throw new UnsupportedOperationException();
    }
}