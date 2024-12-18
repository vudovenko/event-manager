package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.dto.EventUpdateRequestDto;
import dev.vudovenko.eventmanagement.events.exceptions.DateEventInPastException;
import dev.vudovenko.eventmanagement.events.exceptions.EventOccupiedPlacesExceedMaxPlacesException;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.exceptions.UserNotEventCreatorException;
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
import dev.vudovenko.eventmanagement.utils.UserTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerUpdateEventTest extends AbstractTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private LocationTestUtils locationTestUtils;
    @Autowired
    private EventTestUtils eventTestUtils;
    @Autowired
    private UserTestUtils userTestUtils;
    @Autowired
    private DtoMapper<Event, EventDto> eventDtoMapper;

    @Test
    void shouldSuccessfullyUpdateEvent() throws Exception {
        Event createdEvent = eventTestUtils.getCreatedEvent();

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName() + "-" + RandomUtils.getRandomInt(),
                createdEvent.getMaxPlaces() + 10,
                createdEvent.getDate().plusDays(1),
                createdEvent.getCost() + 1200,
                createdEvent.getDuration() + 10,
                createdEvent.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String updatedEventDto = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event updatedEvent = eventDtoMapper.toDomain(
                objectMapper.readValue(updatedEventDto, EventDto.class)
        );

        Assertions.assertEquals(updatedEvent.getId(), createdEvent.getId());
        Assertions.assertTrue(eventService.existsById(createdEvent.getId()));
        Assertions.assertEquals(updatedEvent.getName(), eventUpdateRequestDto.name());
        Assertions.assertEquals(updatedEvent.getOwner(), createdEvent.getOwner());
        Assertions.assertEquals(updatedEvent.getMaxPlaces(), eventUpdateRequestDto.maxPlaces());
        Assertions.assertEquals(updatedEvent.getOccupiedPlaces(), createdEvent.getOccupiedPlaces());
        compareDatesWithTruncatedToSeconds(updatedEvent.getDate(), eventUpdateRequestDto.date());
        Assertions.assertEquals(updatedEvent.getCost(), eventUpdateRequestDto.cost());
        Assertions.assertEquals(updatedEvent.getDuration(), eventUpdateRequestDto.duration());
        Assertions.assertEquals(updatedEvent.getLocation().getId(), eventUpdateRequestDto.locationId());
        Assertions.assertEquals(updatedEvent.getStatus(), EventStatus.WAIT_START);
    }

    @Test
    void shouldSuccessfullyUpdateEventWhenUserRoleIsUserAndUserIsEventCreator() throws Exception {
        User eventCreator = userTestUtils.getRegisteredUser();
        Event eventToUpdate = eventTestUtils.getCreatedEvent(eventCreator);

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                eventToUpdate.getName() + "-" + RandomUtils.getRandomInt(),
                eventToUpdate.getMaxPlaces() + 10,
                eventToUpdate.getDate().plusDays(1),
                eventToUpdate.getCost() + 1200,
                eventToUpdate.getDuration() + 10,
                eventToUpdate.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String updatedEventDto = mockMvc
                .perform(
                        put("/events/{id}", eventToUpdate.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(eventCreator))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event updatedEvent = eventDtoMapper.toDomain(
                objectMapper.readValue(updatedEventDto, EventDto.class)
        );


        Assertions.assertEquals(updatedEvent.getId(), eventToUpdate.getId());
        Assertions.assertTrue(eventService.existsById(eventToUpdate.getId()));
        Assertions.assertEquals(updatedEvent.getName(), eventUpdateRequestDto.name());
        Assertions.assertEquals(updatedEvent.getOwner(), eventCreator);
        Assertions.assertEquals(updatedEvent.getMaxPlaces(), eventUpdateRequestDto.maxPlaces());
        Assertions.assertEquals(updatedEvent.getOccupiedPlaces(), eventToUpdate.getOccupiedPlaces());
        Assertions.assertEquals(updatedEvent.getDate(), eventUpdateRequestDto.date());
        Assertions.assertEquals(updatedEvent.getCost(), eventUpdateRequestDto.cost());
        Assertions.assertEquals(updatedEvent.getDuration(), eventUpdateRequestDto.duration());
        Assertions.assertEquals(updatedEvent.getLocation().getId(), eventUpdateRequestDto.locationId());
        Assertions.assertEquals(updatedEvent.getStatus(), EventStatus.WAIT_START);
    }

    @Test
    void shouldReturnUnauthorizedWhenUpdateEventWithoutAuthorization() throws Exception {
        mockMvc
                .perform(put("/events/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotUpdateEventWhenUserIsNotCreatorAndHasNoAdminRole() throws Exception {
        User eventCreator = userTestUtils.getRegisteredUser();

        Event createdEvent = eventTestUtils.getCreatedEvent(eventCreator);

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName() + "-" + RandomUtils.getRandomInt(),
                createdEvent.getMaxPlaces() + 10,
                createdEvent.getDate().plusDays(1),
                createdEvent.getCost() + 1200,
                createdEvent.getDuration() + 10,
                createdEvent.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event notUpdatedEvent = eventService.findById(createdEvent.getId());

        Assertions.assertEquals(createdEvent.getId(), notUpdatedEvent.getId());
        Assertions.assertEquals(createdEvent.getName(), notUpdatedEvent.getName());
        Assertions.assertEquals(createdEvent.getOwner().getId(), notUpdatedEvent.getOwner().getId());
        Assertions.assertEquals(createdEvent.getMaxPlaces(), notUpdatedEvent.getMaxPlaces());
        Assertions.assertEquals(createdEvent.getOccupiedPlaces(), notUpdatedEvent.getOccupiedPlaces());
        LocalDateTime firstDate = createdEvent.getDate();
        LocalDateTime secondDate = notUpdatedEvent.getDate();
        compareDatesWithTruncatedToSeconds(firstDate, secondDate);
        Assertions.assertEquals(createdEvent.getCost(), notUpdatedEvent.getCost());
        Assertions.assertEquals(createdEvent.getDuration(), notUpdatedEvent.getDuration());
        Assertions.assertEquals(createdEvent.getLocation().getId(), notUpdatedEvent.getLocation().getId());
        Assertions.assertEquals(createdEvent.getStatus(), notUpdatedEvent.getStatus());

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(errorMessageResponse.message(),
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
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldAllowChangingToNewLocationIfCapacityIsSufficient(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);

        Location location = locationTestUtils.getCreatedLocationWithCapacity(100);
        Event createdEvent1 = eventTestUtils
                .getCreatedEvent(
                        30,
                        50,
                        location,
                        owner
                );

        Location locationWithNewCapacity = locationTestUtils.getCreatedLocationWithCapacity(200);
        Event createdEvent2 = eventTestUtils.getCreatedEvent(
                30,
                50,
                locationWithNewCapacity,
                owner
        );

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent1.getName(),
                createdEvent1.getMaxPlaces(),
                createdEvent1.getDate(),
                createdEvent1.getCost(),
                createdEvent1.getDuration(),
                locationWithNewCapacity.getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String updatedEventDto = mockMvc
                .perform(
                        put("/events/{id}", createdEvent1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event updatedEvent = eventDtoMapper.toDomain(
                objectMapper.readValue(updatedEventDto, EventDto.class)
        );

        Assertions.assertTrue(eventService.existsById(createdEvent1.getId()));
        Assertions.assertEquals(updatedEvent.getId(), createdEvent1.getId());
        Assertions.assertEquals(updatedEvent.getLocation().getId(), locationWithNewCapacity.getId());
        Assertions.assertEquals(updatedEvent.getName(), createdEvent1.getName());
        Assertions.assertEquals(updatedEvent.getOwner(), createdEvent1.getOwner());
        Assertions.assertEquals(updatedEvent.getMaxPlaces(), createdEvent1.getMaxPlaces());
        Assertions.assertEquals(updatedEvent.getOccupiedPlaces(), createdEvent1.getOccupiedPlaces());
        Assertions.assertEquals(updatedEvent.getDate(), createdEvent1.getDate());
        Assertions.assertEquals(updatedEvent.getCost(), createdEvent1.getCost());
        Assertions.assertEquals(updatedEvent.getDuration(), createdEvent1.getDuration());
        Assertions.assertEquals(updatedEvent.getStatus(), createdEvent1.getStatus());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotAllowChangingToNewLocationIfCapacityIsInsufficient(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);

        Location location = locationTestUtils.getCreatedLocationWithCapacity(100);
        Event createdEvent1 = eventTestUtils.getCreatedEvent(
                40,
                45,
                location,
                owner
        );

        Location locationWithNewCapacity = locationTestUtils
                .getCreatedLocationWithCapacity(60);
        Event createdEvent2 = eventTestUtils.getCreatedEvent(
                40,
                45,
                locationWithNewCapacity,
                owner
        );

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent1.getName(),
                createdEvent1.getMaxPlaces(),
                createdEvent1.getDate(),
                createdEvent1.getCost(),
                createdEvent1.getDuration(),
                locationWithNewCapacity.getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
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
                ExceptionHandlerMessages.INSUFFICIENT_SEATS.getMessage()
        );

        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                InsufficientSeatsException.MESSAGE_TEMPLATE.formatted(
                        locationWithNewCapacity.getCapacity() - createdEvent2.getMaxPlaces()
                )
        );

        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );

        Event notUpdatedEvent = eventService.findById(createdEvent1.getId());
        Assertions.assertEquals(notUpdatedEvent.getLocation().getId(), location.getId());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldUpdateEventIfLocationCapacityAccommodatesEventMaxPlaces(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Location location = locationTestUtils.getCreatedLocationWithCapacity(100);
        Event createdEvent1 = eventTestUtils.getCreatedEvent(
                40,
                45,
                location,
                owner
        );
        Event createdEvent2 = eventTestUtils.getCreatedEvent(
                20,
                30,
                location,
                owner
        );

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent1.getName(),
                70,
                createdEvent1.getDate(),
                createdEvent1.getCost(),
                createdEvent1.getDuration(),
                createdEvent1.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String updatedEventDto = mockMvc
                .perform(
                        put("/events/{id}", createdEvent1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event updatedEvent = eventDtoMapper.toDomain(
                objectMapper.readValue(updatedEventDto, EventDto.class)
        );

        Assertions.assertTrue(eventService.existsById(createdEvent1.getId()));
        Assertions.assertEquals(updatedEvent.getId(), createdEvent1.getId());

        Assertions.assertEquals(updatedEvent.getMaxPlaces(), eventUpdateRequestDto.maxPlaces());

        assertThat(updatedEvent)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .ignoringFields("maxPlaces")
                .isEqualTo(createdEvent1);

        compareDatesWithTruncatedToSeconds(updatedEvent.getDate(), createdEvent1.getDate());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotUpdateEventIfLocationCapacityDoesNotAccommodateEventMaxPlaces(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Location location = locationTestUtils.getCreatedLocationWithCapacity(100);
        Event createdEvent1 = eventTestUtils.getCreatedEvent(
                40,
                45,
                location,
                owner
        );
        Event createdEvent2 = eventTestUtils.getCreatedEvent(
                20,
                30,
                location,
                owner
        );

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent1.getName(),
                100,
                createdEvent1.getDate(),
                createdEvent1.getCost(),
                createdEvent1.getDuration(),
                createdEvent1.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
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
                ExceptionHandlerMessages.INSUFFICIENT_SEATS.getMessage()
        );

        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                InsufficientSeatsException.MESSAGE_TEMPLATE.formatted(
                        location.getCapacity() - createdEvent2.getMaxPlaces()
                )
        );

        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );

        Event notUpdatedEvent = eventService.findById(createdEvent1.getId());
        Assertions.assertEquals(notUpdatedEvent.getMaxPlaces(), createdEvent1.getMaxPlaces());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldUpdateEventIfMaxPlacesAccommodatesOccupiedPlaces(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Location location = locationTestUtils.getCreatedLocationWithCapacity(100);
        Event createdEvent = eventTestUtils.getCreatedEvent(
                40,
                60,
                location,
                owner
        );

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName(),
                50,
                createdEvent.getDate(),
                createdEvent.getCost(),
                createdEvent.getDuration(),
                createdEvent.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String updatedEventDto = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event updatedEvent = eventDtoMapper.toDomain(
                objectMapper.readValue(updatedEventDto, EventDto.class)
        );

        Assertions.assertEquals(updatedEvent.getMaxPlaces(), eventUpdateRequestDto.maxPlaces());

        assertThat(updatedEvent)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .ignoringFields("maxPlaces")
                .isEqualTo(createdEvent);

        compareDatesWithTruncatedToSeconds(updatedEvent.getDate(), createdEvent.getDate());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotUpdateEventIfMaxPlacesDoesNotAccommodateEventOccupiedPlaces(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Location location = locationTestUtils.getCreatedLocationWithCapacity(100);
        Event createdEvent = eventTestUtils.getCreatedEvent(
                40,
                60,
                location,
                owner
        );

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName(),
                10,
                createdEvent.getDate(),
                createdEvent.getCost(),
                createdEvent.getDuration(),
                createdEvent.getLocation().getId()
        );

        String eventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
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
                ExceptionHandlerMessages.EVENT_OCCUPIED_PLACES_EXCEEDED_MAXIMUM_CAPACITY.getMessage()
        );

        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                EventOccupiedPlacesExceedMaxPlacesException.MESSAGE_TEMPLATE.formatted(
                        createdEvent.getName(),
                        createdEvent.getOccupiedPlaces(),
                        eventUpdateRequestDto.maxPlaces()
                )
        );

        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );

        Event notUpdatedEvent = eventService.findById(createdEvent.getId());
        Assertions.assertEquals(notUpdatedEvent.getMaxPlaces(), createdEvent.getMaxPlaces());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotUpdateEventWhenRequestNotValid(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Event createdEvent = eventTestUtils.getCreatedEvent(owner);

        EventUpdateRequestDto wrongEventUpdateRequestDto
                = eventTestUtils.getWrongEventUpdateRequestDto();

        String wrongEventUpdateRequestDtoJson
                = objectMapper.writeValueAsString(wrongEventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongEventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

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

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotUpdateEventWhenLocationNotExists(String defaultUserLogin) throws Exception {
        Long nonExistentId = Long.MAX_VALUE;

        User owner = userService.findByLogin(defaultUserLogin);
        Event createdEvent = eventTestUtils.getCreatedEvent(owner);

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName(),
                createdEvent.getMaxPlaces(),
                createdEvent.getDate(),
                createdEvent.getCost(),
                createdEvent.getDuration(),
                nonExistentId
        );

        String eventUpdateRequestDtoJson = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(owner))
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
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                LocationNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentId)
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotUpdateEventWhenDateEventInPast(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Event createdEvent = eventTestUtils.getCreatedEvent(owner);

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName(),
                createdEvent.getMaxPlaces(),
                LocalDateTime.now().minusDays(10),
                createdEvent.getCost(),
                createdEvent.getDuration(),
                createdEvent.getLocation().getId()
        );

        String eventUpdateRequestDtoJson = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
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
                ExceptionHandlerMessages.DATE_EVENT_IN_PAST.getMessage()
        );
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                DateEventInPastException.MESSAGE_TEMPLATE.formatted(eventUpdateRequestDto.date())
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#defaultUserLoginsProvider")
    void shouldNotUpdateEventWhenDateFormatIsIncorrect(String defaultUserLogin) throws Exception {
        User owner = userService.findByLogin(defaultUserLogin);
        Event createdEvent = eventTestUtils.getCreatedEvent(owner);

        EventUpdateRequestDto eventUpdateRequestDto = new EventUpdateRequestDto(
                createdEvent.getName(),
                createdEvent.getMaxPlaces(),
                null,
                createdEvent.getCost(),
                createdEvent.getDuration(),
                createdEvent.getLocation().getId()
        );

        String eventUpdateRequestDtoJson = objectMapper.writeValueAsString(eventUpdateRequestDto);

        String incorrectDate = "20-dfs01-d23Td04:56:07.000+00:00";
        eventUpdateRequestDtoJson = eventUpdateRequestDtoJson
                .replace("null", "\"" + incorrectDate + "\"");

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/events/{id}", createdEvent.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventUpdateRequestDtoJson)
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
                ExceptionHandlerMessages.DATE_TIME_PARSE_EXCEPTION.getMessage()
        );
        Assertions.assertTrue(errorMessageResponse.detailedMessage().contains(incorrectDate));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}
