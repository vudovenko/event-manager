package dev.vudovenko.eventmanagement.locations.services;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.locations.repositories.LocationRepository;
import dev.vudovenko.eventmanagement.locations.services.impl.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Location updateLocation(Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException(id);
        }
        locationRepository.deleteById(id);
    }
}
