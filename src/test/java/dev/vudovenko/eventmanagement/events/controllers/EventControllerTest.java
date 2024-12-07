package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.exceptions.CannotDeleteStartedEventException;
import dev.vudovenko.eventmanagement.events.exceptions.DateEventInPastException;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.exceptions.UserNotEventCreatorException;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
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
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest extends AbstractTest {

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

        Event eventToCreate = eventTestUtils.getCreatedEvent(eventCreator);

        mockMvc
                .perform(
                        delete("/events/{id}", eventToCreate.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(eventCreator))
                )
                .andExpect(status().isNoContent());

        Assertions.assertTrue(eventService.existsById(eventToCreate.getId()));

        Event cancelledEvent = eventService.findById(eventToCreate.getId());
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

        Event eventToCreate = eventTestUtils.getCreatedEvent(eventCreator);

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/events/{id}", eventToCreate.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertTrue(eventService.existsById(eventToCreate.getId()));

        Event notCancelledEvent = eventService.findById(eventToCreate.getId());
        Assertions.assertEquals(notCancelledEvent.getStatus(), EventStatus.WAIT_START);

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.USER_NOT_EVENT_CREATOR.getMessage()
        );
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                UserNotEventCreatorException.MESSAGE_TEMPLATE
                        .formatted(eventCreator.getId(), eventToCreate.getName())
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
                LocationNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentEventId));
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
                        .formatted(createdEvent.getName(), createdEvent.getDate())
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
                        .formatted(createdEvent.getName(), createdEvent.getDate())
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}