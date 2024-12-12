package dev.vudovenko.eventmanagement.eventRegistrations.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.AlreadyRegisteredForEventException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationException;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.exceptions.EventNotFoundException;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import dev.vudovenko.eventmanagement.utils.UserTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventRegistrationControllerRegistrationTest extends AbstractTest {

    @Autowired
    private UserService userService;
    @Autowired
    private EventService eventService;
    @Autowired
    private EventRegistrationService eventRegistrationService;
    @Autowired
    private UserTestUtils userTestUtils;
    @Autowired
    private EventTestUtils eventTestUtils;

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.EventTestDataProviders#eventStatusesValidForRegisterProvider")
    void shouldRegisterForEvent(EventStatus eventStatus) throws Exception {
        User user = userTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent(eventStatus);

        mockMvc
                .perform(
                        post("/events/registrations/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                );

        Assertions.assertTrue(eventRegistrationService.isUserRegisteredForEvent(user.getId(), event.getId()));

        EventRegistration eventRegistration = eventRegistrationService
                .findByUserIdAndEventId(user.getId(), event.getId());

        Assertions.assertEquals(user, eventRegistration.getUser());
        Assertions.assertEquals(event, eventRegistration.getEvent());
        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces() + 1
        );
    }

    @Test
    void shouldReturnForbiddenWhenAdminRegisterForEvent() throws Exception {
        User admin = userService.findByLogin(DefaultUserInitializer.DEFAULT_ADMIN_LOGIN);
        Event event = eventTestUtils.getCreatedEvent();

        mockMvc
                .perform(
                        post("/events/registrations/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(admin))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenRegisterForEventWithoutAuthorization() throws Exception {
        mockMvc
                .perform(
                        post("/events/registrations/{eventId}", 1L)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundWhenRegisterForNonExistingEvent() throws Exception {
        Long nonExistentId = Long.MAX_VALUE;
        User user = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events/registrations/{eventId}", nonExistentId)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
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
                EventNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentId));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );

        Assertions.assertFalse(eventRegistrationService.isUserRegisteredForEvent(user.getId(), nonExistentId));
    }

    @Test
    void shouldNotRegisterForEventTwice() throws Exception {
        User user = userTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent();

        mockMvc
                .perform(
                        post("/events/registrations/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                );

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events/registrations/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertTrue(eventRegistrationService.isUserRegisteredForEvent(user.getId(), event.getId()));
        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces() + 1
        );

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.EVENT_REGISTRATION_ALREADY_EXISTS.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                AlreadyRegisteredForEventException.MESSAGE_TEMPLATE.formatted(event.getId()));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.EventTestDataProviders#eventStatusesNotValidForRegisterProvider")
    void shouldNotRegisterForEventWhenEventStatusDoesntAllow(EventStatus eventStatus) throws Exception {
        User user = userTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent(eventStatus);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events/registrations/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.EVENT_STATUS_NOT_ALLOWED_FOR_REGISTRATION.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                EventStatusNotAllowedForRegistrationException.MESSAGE_TEMPLATE
                        .formatted(event.getId(), event.getStatus()));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );

        Assertions.assertFalse(
                eventRegistrationService
                        .isUserRegisteredForEvent(user.getId(), event.getId())
        );
        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces()
        );
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.EventTestDataProviders#eventStatusesValidForRegisterProvider")
    void shouldNotRegisterForEventWhenThereAreNoPlacesAtEvent(EventStatus eventStatus) throws Exception {
        User user = userTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent(10, 10, eventStatus);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events/registrations/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertFalse(eventRegistrationService
                .isUserRegisteredForEvent(user.getId(), event.getId()));

        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces()
        );

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.INSUFFICIENT_SEATS.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                InsufficientSeatsException.MESSAGE_TEMPLATE.formatted(
                        event.getMaxPlaces() - event.getOccupiedPlaces()
                )
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}