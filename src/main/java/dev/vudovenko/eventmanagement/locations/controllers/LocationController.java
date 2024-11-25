package dev.vudovenko.eventmanagement.locations.controllers;

import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/locations")
public class LocationController {

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
        throw new UnsupportedOperationException();
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
