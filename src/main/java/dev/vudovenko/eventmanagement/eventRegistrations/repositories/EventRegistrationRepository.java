package dev.vudovenko.eventmanagement.eventRegistrations.repositories;

import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistrationEntity, Long> {
}
