package dev.vudovenko.eventmanagement.eventRegistrations.mappers;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class EventRegistrationEntityMapper implements EntityMapper<EventRegistration, EventRegistrationEntity> {

    private final EntityMapper<User, UserEntity> userEntityMapper;
    private final EntityMapper<Event, EventEntity> eventEntityMapper;

    public EventRegistrationEntityMapper(
            EntityMapper<User, UserEntity> userEntityMapper,
            @Lazy EntityMapper<Event, EventEntity> eventEntityMapper
    ) {
        this.userEntityMapper = userEntityMapper;
        this.eventEntityMapper = eventEntityMapper;
    }

    @Override
    public EventRegistrationEntity toEntity(EventRegistration eventRegistration) {
        return new EventRegistrationEntity(
                eventRegistration.getId(),
                userEntityMapper.toEntity(eventRegistration.getUser()),
                eventEntityMapper.toEntity(eventRegistration.getEvent())
        );
    }

    @Override
    public EventRegistration toDomain(EventRegistrationEntity eventRegistrationEntity) {
        // todo возможно, стоит вынести в отдельный метод
        User user =
                Hibernate.isInitialized(eventRegistrationEntity.getUser())
                        ? userEntityMapper.toDomain(eventRegistrationEntity.getUser())
                        : new User(eventRegistrationEntity.getUser().getId());
        Event event =
                Hibernate.isInitialized(eventRegistrationEntity.getEvent())
                        ? eventEntityMapper.toDomain(eventRegistrationEntity.getEvent())
                        : new Event(eventRegistrationEntity.getEvent().getId());

        return new EventRegistration(
                eventRegistrationEntity.getId(),
                user,
                event
        );
    }
}
