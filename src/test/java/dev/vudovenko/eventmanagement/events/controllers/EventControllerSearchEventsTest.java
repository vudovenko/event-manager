package dev.vudovenko.eventmanagement.events.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.dto.EventSearchRequestDto;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerSearchEventsTest extends AbstractTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;
    @Autowired
    private EventTestUtils eventTestUtils;
    @Autowired
    private UserTestUtils userTestUtils;
    @Autowired
    private LocationTestUtils locationTestUtils;
    @Autowired
    private DtoMapper<Event, EventDto> eventDtoMapper;
    @Autowired
    private EventSearchRequestDtoTestUtils eventSearchRequestDtoTestUtils;

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#rolesProvider")
    void shouldSearchEvents(UserRole role) throws Exception {
        eventRegistrationRepository.deleteAll();
        eventRepository.deleteAll();

        IntStream.range(0, 10)
                .forEach(i -> eventTestUtils.getCreatedEvent());
        Location location = locationTestUtils.getCreatedLocation();
        String eventToSearchName = "event-to-search-" + RandomUtils.getRandomInt();
        String firstEventName = "first-" + eventToSearchName;
        String secondEventName = "second-" + eventToSearchName;

        Event event1 = eventService.createEvent(
                new Event(
                        null,
                        firstEventName,
                        userTestUtils.getRegisteredUser(),
                        10,
                        3,
                        LocalDateTime.now().plusDays(1),
                        300,
                        45,
                        location,
                        EventStatus.WAIT_START
                )
        );

        Event event2 = eventService.createEvent(
                new Event(
                        null,
                        secondEventName,
                        userTestUtils.getRegisteredUser(),
                        50,
                        30,
                        LocalDateTime.now().plusDays(2),
                        2000,
                        600,
                        location,
                        EventStatus.WAIT_START
                )
        );

        EventSearchRequestDto eventSearchRequestDto = new EventSearchRequestDto(
                eventToSearchName,
                9,
                51,
                LocalDateTime.now().plusDays(1).minusMinutes(15),
                LocalDateTime.now().plusDays(2).plusMinutes(15),
                299,
                2001,
                44,
                601,
                location.getId(),
                EventStatus.WAIT_START
        );

        String eventSearchRequestDtoJson = objectMapper.writeValueAsString(eventSearchRequestDto);

        String foundEventsJson = mockMvc
                .perform(
                        post("/events/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventSearchRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(role))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<EventDto> foundEventsDto = objectMapper.readValue(
                foundEventsJson,
                new TypeReference<>() {
                }
        );
        List<Event> foundEvents = foundEventsDto
                .stream()
                .map(eventDtoMapper::toDomain)
                .toList();

        Assertions.assertEquals(foundEventsDto.size(), 2);
        Event eventFromSearch1 = foundEvents.get(0);
        Event eventFromSearch2 = foundEvents.get(1);
        org.assertj.core.api.Assertions.assertThat(eventFromSearch1)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(event1);
        org.assertj.core.api.Assertions.assertThat(eventFromSearch2)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(event2);

        compareDatesWithTruncatedToSeconds(event1.getDate(), eventFromSearch1.getDate());
        compareDatesWithTruncatedToSeconds(event2.getDate(), eventFromSearch2.getDate());
    }

    @Test
    void shouldReturnUnauthorizedWhenSearchEventWithoutAuthorization() throws Exception {
        mockMvc
                .perform(post("/events/search"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#rolesProvider")
    void shouldFindAllEventsWithEmptyFilter(UserRole role) throws Exception {
        eventRegistrationRepository.deleteAll();
        eventRepository.deleteAll();

        int numberEvents = 100;
        IntStream.range(0, numberEvents)
                .forEach(i -> eventTestUtils.getCreatedEvent());

        EventSearchRequestDto eventSearchRequestDto = eventSearchRequestDtoTestUtils
                .getBlankEventSearchRequestDto();

        String eventSearchRequestDtoJson = objectMapper.writeValueAsString(eventSearchRequestDto);

        String foundEventsJson = mockMvc
                .perform(
                        post("/events/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(eventSearchRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(role))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<EventDto> foundEventsDto = objectMapper.readValue(
                foundEventsJson,
                new TypeReference<>() {
                }
        );

        List<Event> foundEvents = foundEventsDto
                .stream()
                .map(eventDtoMapper::toDomain)
                .toList();

        Assertions.assertEquals(foundEventsDto.size(), numberEvents);
    }

    @ParameterizedTest
    @MethodSource("dev.vudovenko.eventmanagement.utils.dataProviders.UserTestDataProviders#rolesProvider")
    void shouldNotSearchEventsWhenNotValid(UserRole role) throws Exception {
        EventSearchRequestDto notValidEventCreateRequestDto = eventSearchRequestDtoTestUtils
                .getNotValidEventCreateRequestDto();

        String notValidEventCreateRequestDtoJson = objectMapper
                .writeValueAsString(notValidEventCreateRequestDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/events/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(notValidEventCreateRequestDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(role))
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

        Assertions.assertTrue(detailedMessage.contains("placesMin:"));
        Assertions.assertTrue(detailedMessage.contains("placesMax:"));
        Assertions.assertTrue(detailedMessage.contains("costMin:"));
        Assertions.assertTrue(detailedMessage.contains("costMax:"));
        Assertions.assertTrue(detailedMessage.contains("durationMin:"));
        Assertions.assertTrue(detailedMessage.contains("durationMax:"));

        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}
