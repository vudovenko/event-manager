package dev.vudovenko.eventmanagement.events.repositories;

import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    Optional<EventEntity> findByIdWithOwner(Long eventId);
}
