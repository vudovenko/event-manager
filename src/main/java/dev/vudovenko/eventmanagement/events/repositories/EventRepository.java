package dev.vudovenko.eventmanagement.events.repositories;

import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    @Query(
            """
                    SELECT e
                    FROM EventEntity e
                    LEFT JOIN FETCH e.owner
                    WHERE e.id = :eventId
                    """
    )
    Optional<EventEntity> findByIdWithOwner(@Param("eventId") Long eventId);

    @Query(
            """
                    SELECT e
                    FROM EventEntity e
                    WHERE (:name IS NULL OR e.name LIKE CONCAT('%', :name, '%'))
                    AND (:placesMin IS NULL OR e.maxPlaces >= :placesMin)
                    AND (:placesMax IS NULL OR e.maxPlaces <= :placesMax)
                    AND (CAST(:dateStartAfter AS timestamp) IS NULL OR e.date >= :dateStartAfter)
                    AND (CAST(:dateStartBefore AS timestamp) IS NULL OR e.date <= :dateStartBefore)
                    AND (:costMin IS NULL OR e.cost >= :costMin)
                    AND (:costMax IS NULL OR e.cost <= :costMax)
                    AND (:durationMin IS NULL OR e.duration >= :durationMin)
                    AND (:durationMax IS NULL OR e.duration <= :durationMax)
                    AND (:locationId IS NULL OR e.location.id = :locationId)
                    AND (:eventStatus IS NULL OR e.status = :eventStatus)
                    """
    )
    List<EventEntity> searchEvents(
            @Param("name") String name,
            @Param("placesMin") Integer placesMin,
            @Param("placesMax") Integer placesMax,
            @Param("dateStartAfter") LocalDateTime dateStartAfter,
            @Param("dateStartBefore") LocalDateTime dateStartBefore,
            @Param("costMin") Integer costMin,
            @Param("costMax") Integer costMax,
            @Param("durationMin") Integer durationMin,
            @Param("durationMax") Integer durationMax,
            @Param("locationId") Long locationId,
            @Param("eventStatus") EventStatus eventStatus
    );

    List<EventEntity> findAllByOwner(UserEntity eventOwner);

    @Modifying
    @Query(
            """
                    UPDATE EventEntity e
                    SET e.occupiedPlaces = e.occupiedPlaces + 1
                    WHERE e.id = :eventId
                    """
    )
    void increaseOccupiedPlaces(Long eventId);

    @Modifying
    @Query(
            """
                    UPDATE EventEntity e
                    SET e.occupiedPlaces = e.occupiedPlaces - 1
                    WHERE e.id = :eventId
                    """
    )
    void decreaseOccupiedPlaces(Long eventId);

    @Query(
            value = """
                    UPDATE events e
                    SET status = 'STARTED'
                    WHERE e.date <= CAST(:currentTime AS timestamp)
                    AND e.status = 'WAIT_START'
                    RETURNING e.*
                    """,
            nativeQuery = true
    )
    List<EventEntity> updateEventStatusWhenItStarted(LocalDateTime currentTime);


    List<EventEntity> findAllByDateLessThanEqualAndStatus(LocalDateTime currentTime, EventStatus status);

    @Query(
            value = """
                    UPDATE events e
                    SET status = 'FINISHED'
                    WHERE (e.date + (e.duration * INTERVAL '1 minute')) <= CAST(:currentTime AS timestamp)
                    AND e.status = 'STARTED'
                    RETURNING e.*
                    """,
            nativeQuery = true
    )
    List<EventEntity> updateEventStatusWhenItIsOver(LocalDateTime currentTime);

    @Query(
            value = """
                    SELECT *
                    FROM events e
                    WHERE (e.date + (e.duration * INTERVAL '1 minute')) <= CAST(:currentTime AS timestamp)
                    AND e.status = 'STARTED'
                    """,
            nativeQuery = true
    )
    List<EventEntity> findAllEventsToFinish(LocalDateTime currentTime);

    @Query(
            """
                    SELECT eReg.user.id
                    FROM EventRegistrationEntity eReg
                    WHERE eReg.event.id = :eventId
                    """
    )
    List<Long> getEventParticipants(@Param("eventId") Long eventId);
}
