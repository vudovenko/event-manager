package dev.vudovenko.eventmanagement.scheduler;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class EventStatusSchedulerTest extends AbstractTest {

    @Autowired
    private EventStatusScheduler eventStatusScheduler;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventTestUtils eventTestUtils;

    @Autowired
    private EntityMapper<Event, EventEntity> eventEntityMapper;

    @Test
    void shouldUpdateStatusesFromWaitStartToStarted() {
        eventRegistrationRepository.deleteAll();
        eventRepository.deleteAll();

        Random random = new Random();
        Set<Event> events = IntStream.range(0, 10)
                .mapToObj(i -> {
                            EventEntity createdEvent = eventTestUtils
                                    .getCreatedEvent(
                                            EventStatus.WAIT_START,
                                            random.nextInt(100) + 30,
                                            LocalDateTime.now()
                                    );
                            return eventEntityMapper.toDomain(createdEvent);
                        }
                )
                .collect(Collectors.toSet());
        eventTestUtils.getCreatedEvent(EventStatus.STARTED, 100, LocalDateTime.now());
        eventTestUtils.getCreatedEvent(EventStatus.CANCELLED, 100, LocalDateTime.now().minusHours(1));
        eventTestUtils.getCreatedEvent(EventStatus.FINISHED, 100, LocalDateTime.now().minusDays(1));

        eventStatusScheduler.updateEventStatuses();

        Set<Event> updatedEvents = eventRepository.findAll()
                .stream()
                .map(eventEntityMapper::toDomain)
                .collect(Collectors.toSet());

        Assertions.assertEquals(
                updatedEvents
                        .stream()
                        .filter(event -> event.getStatus() == EventStatus.STARTED)
                        .count() - 1, events.size()
        );
        Assertions.assertTrue(updatedEvents.containsAll(events));
    }

    @Test
    void shouldUpdateStatusesFromStartedToFinished() {
        eventRegistrationRepository.deleteAll();
        eventRepository.deleteAll();

        Random random = new Random();
        Set<Event> events = IntStream.range(0, 10)
                .mapToObj(i -> {
                            EventEntity createdEvent = eventTestUtils
                                    .getCreatedEvent(
                                            EventStatus.STARTED,
                                            random.nextInt(100) + 30,
                                            LocalDateTime.now().minusHours(3)
                                    );
                            return eventEntityMapper.toDomain(createdEvent);
                        }
                )
                .collect(Collectors.toSet());
        eventTestUtils.getCreatedEvent(EventStatus.WAIT_START, 100, LocalDateTime.now().plusDays(1));
        eventTestUtils.getCreatedEvent(EventStatus.CANCELLED, 100, LocalDateTime.now().minusHours(1));
        eventTestUtils.getCreatedEvent(EventStatus.FINISHED, 100, LocalDateTime.now().minusDays(1));

        eventStatusScheduler.updateEventStatuses();

        Set<Event> updatedEvents = eventRepository.findAll()
                .stream()
                .map(eventEntityMapper::toDomain)
                .collect(Collectors.toSet());

        Assertions.assertEquals(
                updatedEvents
                        .stream()
                        .filter(event -> event.getStatus() == EventStatus.FINISHED)
                        .count() - 1, events.size()
        );
        Assertions.assertTrue(updatedEvents.containsAll(events));
    }
}