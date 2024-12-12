package dev.vudovenko.eventmanagement.events.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerGetMyEventTest extends AbstractTest {

    @Autowired
    private UserService userService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventTestUtils eventTestUtils;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private DtoMapper<Event, EventDto> eventDtoMapper;

    @Test
    void shouldGetMyEvent() throws Exception {
        eventRegistrationRepository.deleteAll();
        eventRepository.deleteAll();

        IntStream.range(0, 20)
                .forEach(i -> eventTestUtils.getCreatedEvent());

        User user = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);

        Set<Event> events = IntStream.range(0, 5)
                .mapToObj(i -> eventTestUtils.getCreatedEvent(user))
                .collect(Collectors.toSet());

        String myEventsJson = mockMvc
                .perform(
                        get("/events/my")
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(user))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Set<EventDto> myEventsDtos = objectMapper.readValue(
                myEventsJson,
                new TypeReference<>() {
                }
        );

        Set<Event> myEvents = myEventsDtos.stream()
                .map(eventDtoMapper::toDomain)
                .collect(Collectors.toSet());

        Assertions.assertEquals(events.size(), myEvents.size());
        Assertions.assertTrue(events.containsAll(myEvents));
    }

    @Test
    void shouldReturnForbiddenWhenAdminGetHisEvents() throws Exception {
        mockMvc
                .perform(
                        get("/events/my")
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenGetMyEventsWithoutAuthorization() throws Exception {
        mockMvc
                .perform(get("/events/my"))
                .andExpect(status().isUnauthorized());
    }
}
