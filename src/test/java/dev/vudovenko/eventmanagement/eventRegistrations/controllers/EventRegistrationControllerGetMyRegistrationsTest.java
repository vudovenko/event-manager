package dev.vudovenko.eventmanagement.eventRegistrations.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.EventRegistrationTestUtils;
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

public class EventRegistrationControllerGetMyRegistrationsTest extends AbstractTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EventRegistrationService eventRegistrationService;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventRegistrationTestUtils eventRegistrationTestUtils;
    @Autowired
    private EventTestUtils eventTestUtils;

    @Autowired
    private DtoMapper<Event, EventDto> eventDtoMapper;

    @Test
    void shouldGetMyRegistrations() throws Exception {
        eventRegistrationRepository.deleteAll();

        IntStream.range(0, 20)
                .forEach(i -> eventRegistrationTestUtils.createEventRegistration());

        User user = userService.findByLogin(DefaultUserInitializer.DEFAULT_USER_LOGIN);

        Set<Event> eventsWithRegistration = IntStream.range(0, 5)
                .mapToObj(i -> {
                    Event event = eventTestUtils.getCreatedEvent();
                    eventRegistrationService.registerForEvent(event.getId(), user);

                    return event;
                })
                .collect(Collectors.toSet());

        String myEventsJson = mockMvc
                .perform(
                        get("/events/registrations/my")
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

        Assertions.assertEquals(eventsWithRegistration.size(), myEvents.size());
        Assertions.assertTrue(eventsWithRegistration.containsAll(myEvents));
    }

    @Test
    void shouldReturnForbiddenWhenAdminGetHisEvents() throws Exception {
        mockMvc
                .perform(
                        get("/events/registrations/my")
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenGetMyEventsWithoutAuthorization() throws Exception {
        mockMvc
                .perform(get("/events/registrations/my"))
                .andExpect(status().isUnauthorized());
    }
}
