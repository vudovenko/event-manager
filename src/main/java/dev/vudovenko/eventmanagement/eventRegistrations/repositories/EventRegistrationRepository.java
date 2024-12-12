package dev.vudovenko.eventmanagement.eventRegistrations.repositories;

import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistrationEntity, Long> {

    Optional<EventRegistrationEntity> findByUserIdAndEventId(Long userId, Long eventId);

    Boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    List<EventRegistrationEntity> findByUserId(Long userId);
}
