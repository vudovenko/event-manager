package dev.vudovenko.eventmanagement.locations.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationCapacityIsLowerThanItWasException;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.locations.repositories.LocationRepository;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final EntityMapper<Location, LocationEntity> locationEntityMapper;

    @Override
    public List<Location> getAllLocations() {
        return locationRepository
                .findAll()
                .stream()
                .map(locationEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Location getById(Long id) {
        LocationEntity locationEntity = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));

        return locationEntityMapper.toDomain(locationEntity);
    }

    @Override
    public Location createLocation(Location location) {
        LocationEntity createdLocationEntity = locationRepository.save(
                locationEntityMapper.toEntity(location)
        );

        return locationEntityMapper.toDomain(createdLocationEntity);
    }

    @Override
    public Location updateLocation(Long id, Location locationToUpdate) {
        Optional<LocationEntity> locationEntityOptional = locationRepository.findById(id);
        Location locationFromDb = locationEntityMapper.toDomain(
                locationEntityOptional
                        .orElseThrow(() -> new LocationNotFoundException(id))
        );
        checkCapacityConstraint(locationFromDb, locationToUpdate);

        locationToUpdate.setId(id);
        LocationEntity updatedLocationEntity = locationRepository.save(
                locationEntityMapper.toEntity(locationToUpdate)
        );

        return locationEntityMapper.toDomain(updatedLocationEntity);
    }

    private static void checkCapacityConstraint(Location oldLocation, Location locationToUpdate) {
        if (locationToUpdate.getCapacity() < oldLocation.getCapacity()) {
            throw new LocationCapacityIsLowerThanItWasException(
                    oldLocation.getCapacity(),
                    locationToUpdate.getCapacity()
            );
        }
    }

    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException(id);
        }
        locationRepository.deleteById(id);
    }
}
