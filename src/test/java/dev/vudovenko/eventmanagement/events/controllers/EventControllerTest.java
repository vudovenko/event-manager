package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.DefaultUserInitializer;
import dev.vudovenko.eventmanagement.users.services.UserService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.util.LocationTestUtils;
import dev.vudovenko.eventmanagement.util.RandomUtils;
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

}