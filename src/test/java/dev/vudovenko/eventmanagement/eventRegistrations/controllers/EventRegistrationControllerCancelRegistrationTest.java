package dev.vudovenko.eventmanagement.eventRegistrations.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventRegistrationNotFoundException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationCancellationException;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.exceptions.EventNotFoundException;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import dev.vudovenko.eventmanagement.utils.UserTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventRegistrationControllerCancelRegistrationTest extends AbstractTest {

    @Autowired
    private UserService userService;
    @Autowired
    private EventService eventService;
    @Autowired
    private EventRegistrationService eventRegistrationService;
    @Autowired
    private EventTestUtils eventTestUtils;
    @Autowired
    private UserTestUtils userTestUtils;

    @Test
    void shouldCancelRegistrationForEvent() throws Exception {
        User user = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);
        Event event = eventTestUtils.getCreatedEvent(EventStatus.WAIT_START);
        eventRegistrationService.registerForEvent(event.getId(), user);
        event.setOccupiedPlaces(event.getOccupiedPlaces() + 1);

        mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                );

        Assertions.assertFalse(eventRegistrationService.isUserRegisteredForEvent(user.getId(), event.getId()));

        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces() - 1
        );
    }

    @Test
    void shouldReturnForbiddenWhenAdminCancelRegistrationForEvent() throws Exception {
        User admin = userService.findByLogin(DefaultUserInitializer.DEFAULT_ADMIN_LOGIN);

        mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", Long.MAX_VALUE)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(admin))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenCancelRegistrationForEventWithoutAuthorization() throws Exception {
        mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", 1L)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundWhenCancelRegistrationForNonExistingEvent() throws Exception {
        Long nonExistentId = Long.MAX_VALUE;
        User user = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", nonExistentId)
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
    void shouldNotCancelRegistrationForEventTwice() throws Exception {
        User user = userTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent();
        eventRegistrationService.registerForEvent(event.getId(), user);
        event.setOccupiedPlaces(event.getOccupiedPlaces() + 1);

        mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                );

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertFalse(eventRegistrationService.isUserRegisteredForEvent(user.getId(), event.getId()));
        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces() - 1
        );

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.EVENT_REGISTRATION_NOT_FOUND.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                EventRegistrationNotFoundException.MESSAGE_TEMPLATE
                        .formatted(user.getId(), event.getId()));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotCancelRegistrationForEventWhenEventStatusDoesntAllow() throws Exception {
        User user = userTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent(EventStatus.STARTED);
        eventRegistrationService.registerForEvent(event.getId(), user);
        event.setOccupiedPlaces(event.getOccupiedPlaces() + 1);

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/registrations/cancel/{eventId}", event.getId())
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
                ExceptionHandlerMessages.EVENT_STATUS_NOT_ALLOWED_FOR_CANCELLATION.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                EventStatusNotAllowedForRegistrationCancellationException.MESSAGE_TEMPLATE
                        .formatted(event.getId(), event.getStatus()));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );

        Assertions.assertTrue(
                eventRegistrationService
                        .isUserRegisteredForEvent(user.getId(), event.getId())
        );
        Event updatedEvent = eventService.findById(event.getId());
        Assertions.assertEquals(
                updatedEvent.getOccupiedPlaces(),
                event.getOccupiedPlaces()
        );
    }
}
