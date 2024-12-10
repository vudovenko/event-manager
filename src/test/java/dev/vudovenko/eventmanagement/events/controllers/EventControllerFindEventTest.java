package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.exceptions.EventNotFoundException;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.EventTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerFindEventTest extends AbstractTest {

    @Autowired
    private EventTestUtils eventTestUtils;
    @Autowired
    private DtoMapper<Event, EventDto> eventDtoMapper;

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void shouldSuccessfullyFindEventById(UserRole userRole) throws Exception {
        Event eventToFind = eventTestUtils.getCreatedEvent();

        String foundLocationsJson = mockMvc
                .perform(
                        get("/events/{id}", eventToFind.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(userRole))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Event foundEvent = eventDtoMapper.toDomain(
                objectMapper.readValue(foundLocationsJson, EventDto.class)
        );

        assertThat(foundEvent)
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(eventToFind);

        compareDatesWithTruncatedToSeconds(eventToFind.getDate(), foundEvent.getDate());
    }

    @Test
    void shouldReturnUnauthorizedWhenFindEventByIdWithoutAuthorization() throws Exception {
        mockMvc
                .perform(get("/events/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void shouldNotFindEventByNonExistentId(UserRole userRole) throws Exception {
        Long nonExistentId = Long.MAX_VALUE;

        String errorMessageResponseJson = mockMvc
                .perform(
                        get("/events/{id}", nonExistentId)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(userRole)))
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
                EventNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentId)
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }
}
