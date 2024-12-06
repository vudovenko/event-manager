package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.exceptions.DateEventInPastException;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import dev.vudovenko.eventmanagement.utils.LocationTestUtils;
import dev.vudovenko.eventmanagement.utils.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest extends AbstractTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private LocationTestUtils locationTestUtils;
    @Autowired
    private EventTestUtils eventTestUtils;

    @Test
    void shouldSuccessfullyCreateEvent() throws Exception {
        Location createdLocation = locationTestUtils.getCreatedLocation();
        User defaultUser = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);

        EventCreateRequestDto eventCreateRequestDto = new EventCreateRequestDto(
                "event-" + RandomUtils.getRandomInt(),
                100,
                LocalDateTime.now().plusDays(1),
                1200,
                60,
                createdLocation.getId()
        );

        String eventCreateRequestDtoJson = objectMapper.writeValueAsString(eventCreateRequestDto);

        String createdEventJson = mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto createdEventDto = objectMapper.readValue(createdEventJson, EventDto.class);

        Assertions.assertTrue(eventService.existsById(createdEventDto.id()));
        Assertions.assertEquals(createdEventDto.name(), eventCreateRequestDto.name());
        Assertions.assertEquals(createdEventDto.ownerId(), defaultUser.getId());
        Assertions.assertEquals(createdEventDto.maxPlaces(), eventCreateRequestDto.maxPlaces());
        Assertions.assertEquals(createdEventDto.occupiedPlaces(), 0);
        Assertions.assertEquals(createdEventDto.date(), eventCreateRequestDto.date());
        Assertions.assertEquals(createdEventDto.cost(), eventCreateRequestDto.cost());
        Assertions.assertEquals(createdEventDto.duration(), eventCreateRequestDto.duration());
        Assertions.assertEquals(createdEventDto.locationId(), createdLocation.getId());
        Assertions.assertEquals(createdEventDto.status(), EventStatus.WAIT_START);
    }

    @Test
    void shouldReturnForbiddenWhenAdminCreateEvent() throws Exception {
        mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreateEventWithoutAuthorization() throws Exception {
        mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateEventWhenRequestNotValid() throws Exception {
        EventCreateRequestDto wrongEventCreateRequestDto
                = eventTestUtils.getWrongEventCreateRequestDto();

        String wrongEventCreateRequestDtoJson
                = objectMapper.writeValueAsString(wrongEventCreateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongEventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse
                = objectMapper.readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        String detailedMessage = errorMessageResponse.detailedMessage();

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.VALIDATION_FAILED.getMessage()
        );

        Assertions.assertTrue(detailedMessage.contains("name:"));
        Assertions.assertTrue(detailedMessage.contains("maxPlaces:"));
        Assertions.assertTrue(detailedMessage.contains("date:"));
        Assertions.assertTrue(detailedMessage.contains("cost:"));
        Assertions.assertTrue(detailedMessage.contains("duration:"));
        Assertions.assertTrue(detailedMessage.contains("locationId:"));

        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotCreateEventWhenLocationNotExists() throws Exception {
        Long nonExistentId = Long.MAX_VALUE;

        EventCreateRequestDto eventCreateRequestDto = new EventCreateRequestDto(
                "event-" + RandomUtils.getRandomInt(),
                100,
                LocalDateTime.now().plusDays(10),
                100,
                45,
                nonExistentId
        );

        String eventCreateRequestDtoJson = objectMapper.writeValueAsString(eventCreateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
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
                LocationNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentId));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotCreateEventWhenDateEventInPast() throws Exception {
        LocalDateTime dateInPast = LocalDateTime.now().minusDays(10);

        EventCreateRequestDto eventCreateRequestDto = new EventCreateRequestDto(
                "event-" + RandomUtils.getRandomInt(),
                100,
                dateInPast,
                100,
                45,
                1L
        );

        String eventCreateRequestDtoJson = objectMapper.writeValueAsString(eventCreateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.DATE_EVENT_IN_PAST.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                DateEventInPastException.MESSAGE_TEMPLATE.formatted(dateInPast));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotCreateEventWhenDateFormatIsIncorrect() throws Exception {
        EventCreateRequestDto eventCreateRequestDto = new EventCreateRequestDto(
                "event-" + RandomUtils.getRandomInt(),
                100,
                null,
                100,
                45,
                1L
        );

        String eventCreateRequestDtoJson = objectMapper.writeValueAsString(eventCreateRequestDto);

        String incorrectDate = "20-dfs01-d23Td04:56:07.000+00:00";
        eventCreateRequestDtoJson = eventCreateRequestDtoJson
                .replace("null", "\"" + incorrectDate + "\"");

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.DATE_TIME_PARSE_EXCEPTION.getMessage()
        );
        Assertions.assertTrue(errorMessageResponse.detailedMessage().contains(incorrectDate));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotCreateEventWhenThereAreNotEnoughSeatsInLocation() throws Exception {
        Event firstEvent = eventTestUtils.getCreatedEvent();

        Location location = firstEvent.getLocation();
        int availableSeats = location.getCapacity() - firstEvent.getMaxPlaces();

        EventCreateRequestDto secondEvent = new EventCreateRequestDto(
                "event-" + RandomUtils.getRandomInt(),
                availableSeats + 100,
                LocalDateTime.now().plusDays(10),
                100,
                45,
                location.getId()
        );

        String eventCreateRequestDtoJson = objectMapper.writeValueAsString(secondEvent);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.INSUFFICIENT_SEATS.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                InsufficientSeatsException.MESSAGE_TEMPLATE.formatted(availableSeats));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}