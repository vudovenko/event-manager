package dev.vudovenko.eventmanagement.locations.repositories;


import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    @Query(
            """
                    SELECT l.capacity - COALESCE(sum(e.maxPlaces), 0)
                    FROM LocationEntity l
                    LEFT JOIN EventEntity e
                    ON e.location.id = l.id AND e.id != :eventId
                    WHERE l.id = :locationId
                    GROUP BY l.id
                    """
    )
    Integer getNumberAvailableSeatsWithoutTakingIntoAccountEvent(
            @Param("locationId") Long locationId,
            @Param("eventId") Long eventId
    );
}
