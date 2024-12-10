package dev.vudovenko.eventmanagement.users.repositories;

import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByLogin(String login);

    Optional<UserEntity> findByLogin(String login);

    @Query(
            """
                    SELECT e.owner
                    FROM EventEntity e
                    WHERE e.id = :eventId
                    """
    )
    Optional<UserEntity> findByEventId(Long eventId);
}
