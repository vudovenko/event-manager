package dev.vudovenko.eventmanagement.events.repositories;

import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
}
