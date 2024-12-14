package dev.vudovenko.eventmanagement.locations.services.validations;

import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationCapacityIsLowerThanItWasException;
import org.springframework.stereotype.Service;

@Service
public class LocationValidationService {

    public void checkCapacityConstraint(Location oldLocation, Location locationToUpdate) {
        if (locationToUpdate.getCapacity() < oldLocation.getCapacity()) {
            throw new LocationCapacityIsLowerThanItWasException(
                    oldLocation.getCapacity(),
                    locationToUpdate.getCapacity()
            );
        }
    }
}
