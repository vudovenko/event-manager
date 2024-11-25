package dev.vudovenko.eventmanagement.locations.repositories;


import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

}
