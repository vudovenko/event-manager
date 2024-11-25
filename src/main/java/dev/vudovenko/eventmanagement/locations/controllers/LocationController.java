package dev.vudovenko.eventmanagement.locations.controllers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import dev.vudovenko.eventmanagement.locations.services.impl.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    @Qualifier("locationDtoMapper")
    private final DtoMapper<Location, LocationDto> locationDtoMapper;

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        log.info("Get request for get all locations");
        throw new UnsupportedOperationException();
    }


    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getById(@PathVariable Long id) {
        log.info("Get request for get location by id");
        throw new UnsupportedOperationException();
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(
            @Valid @RequestBody LocationDto locationDto
    ) {
        log.info("Get request for create location");
        Location location = locationService.createLocation(
                locationDtoMapper.toDomain(locationDto)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationDtoMapper.toDto(location));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        log.info("Get request for delete location");
        throw new UnsupportedOperationException();
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationDto locationDto
    ) {
        log.info("Get request for update location");
        throw new UnsupportedOperationException();
    }
}
