package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.exceptions.CannotDeleteStartedEventException;
import dev.vudovenko.eventmanagement.events.exceptions.EventAlreadyCancelledException;
import dev.vudovenko.eventmanagement.events.exceptions.EventNotFoundException;
import dev.vudovenko.eventmanagement.events.exceptions.UserNotEventCreatorException;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import dev.vudovenko.eventmanagement.utils.LocationTestUtils;
import dev.vudovenko.eventmanagement.utils.RandomUtils;
import dev.vudovenko.eventmanagement.utils.UserTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerDeleteEventsTest extends AbstractTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private LocationTestUtils locationTestUtils;
    @Autowired
    private EventTestUtils eventTestUtils;
    @Autowired
    private UserTestUtils userTestUtils;
    @Autowired
    private EntityMapper<User, UserEntity> userEntityMapper;

    @Test
    void shouldDeleteEvent() throws Exception {
        Event event = eventTestUtils.getCreatedEvent();

        mockMvc
                .perform(
                        delete("/events/{id}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isNoContent());

        Assertions.assertTrue(eventService.existsById(event.getId()));

        Event cancelledEvent = eventService.findById(event.getId());
        Assertions.assertEquals(cancelledEvent.getStatus(), EventStatus.CANCELLED);
    }

    @Test
    void shouldDeleteEventWhenUserRoleIsUserAndUserIsEventCreator() throws Exception {
        User eventCreator = userTestUtils.getRegisteredUser();

        Event eventToDelete = eventTestUtils.getCreatedEvent(eventCreator);

        mockMvc
                .perform(
                        delete("/events/{id}", eventToDelete.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(eventCreator))
                )
                .andExpect(status().isNoContent());

        Assertions.assertTrue(eventService.existsById(eventToDelete.getId()));

        Event cancelledEvent = eventService.findById(eventToDelete.getId());
        Assertions.assertEquals(cancelledEvent.getStatus(), EventStatus.CANCELLED);
    }

    @Test
    void shouldReturnUnauthorizedWhenDeleteEventWithoutAuthorization() throws Exception {
        Event event = eventTestUtils.getCreatedEvent();

        mockMvc
                .perform(delete("/events/{id}", event.getId()))
                .andExpect(status().isUnauthorized());

        Assertions.assertTrue(eventService.existsById(event.getId()));

        Event cancelledEvent = eventService.findById(event.getId());
        Assertions.assertEquals(cancelledEvent.getStatus(), EventStatus.WAIT_START);
    }

    @Test
    void shouldNotDeleteEventWhenUserIsNotCreatorAndHasNoAdminRole() throws Exception {
        User eventCreator = userTestUtils.getRegisteredUser();

        Event createdEvent = eventTestUtils.getCreatedEvent(eventCreator);

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/{id}", createdEvent.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertTrue(eventService.existsById(createdEvent.getId()));

        Event notCancelledEvent = eventService.findById(createdEvent.getId());
        Assertions.assertEquals(notCancelledEvent.getStatus(), EventStatus.WAIT_START);

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.USER_NOT_EVENT_CREATOR.getMessage()
        );

        User defaultUser = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                UserNotEventCreatorException.MESSAGE_TEMPLATE
                        .formatted(defaultUser.getId(), createdEvent.getId())
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void shouldNotDeleteEventByNonExistentId(UserRole userRole) throws Exception {
        Long nonExistentEventId = Long.MAX_VALUE;

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/{id}", nonExistentEventId)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(userRole))
                )
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.ENTITY_NOT_FOUND.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                EventNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentEventId));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("defaultUserLoginsProvider")
    void shouldNotDeleteEventThatHasAlreadyStarted(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);

        EventEntity createdEvent = eventRepository.save(
                new EventEntity(
                        null,
                        "event-" + RandomUtils.getRandomInt(),
                        userEntityMapper.toEntity(owner),
                        50,
                        30,
                        LocalDateTime.now().minusMinutes(30),
                        1200,
                        60,
                        locationTestUtils.getCreatedLocationEntity(),
                        EventStatus.STARTED
                )
        );

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/{id}", createdEvent.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.CANNOT_DELETE_STARTED_EVENT.getMessage()
        );
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                CannotDeleteStartedEventException.MESSAGE_TEMPLATE
                        .formatted(
                                createdEvent.getName(),
                                createdEvent.getDate().truncatedTo(ChronoUnit.SECONDS)
                        )
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("defaultUserLoginsProvider")
    void shouldNotDeleteEventThatHasAlreadyFinished(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);

        EventEntity createdEvent = eventRepository.save(
                new EventEntity(
                        null,
                        "event-" + RandomUtils.getRandomInt(),
                        userEntityMapper.toEntity(owner),
                        50,
                        30,
                        LocalDateTime.now().minusDays(10),
                        1200,
                        60,
                        locationTestUtils.getCreatedLocationEntity(),
                        EventStatus.FINISHED
                )
        );

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/{id}", createdEvent.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.CANNOT_DELETE_STARTED_EVENT.getMessage()
        );
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                CannotDeleteStartedEventException.MESSAGE_TEMPLATE
                        .formatted(
                                createdEvent.getName(),
                                createdEvent.getDate().truncatedTo(ChronoUnit.SECONDS)
                        )
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("defaultUserLoginsProvider")
    void shouldNotCancelEventThatIsAlreadyCancelled(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);

        EventEntity createdEvent = eventRepository.save(
                new EventEntity(
                        null,
                        "event-" + RandomUtils.getRandomInt(),
                        userEntityMapper.toEntity(owner),
                        50,
                        30,
                        LocalDateTime.now().minusDays(10),
                        1200,
                        60,
                        locationTestUtils.getCreatedLocationEntity(),
                        EventStatus.CANCELLED
                )
        );

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/{id}", createdEvent.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.EVENT_ALREADY_CANCELLED.getMessage()
        );
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                EventAlreadyCancelledException.MESSAGE_TEMPLATE
                        .formatted(createdEvent.getName())
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}
