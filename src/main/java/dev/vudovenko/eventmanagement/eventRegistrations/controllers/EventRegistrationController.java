package dev.vudovenko.eventmanagement.eventRegistrations.controllers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/events/registrations")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;
    private final DtoMapper<Event, EventDto> eventDtoMapper;

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> registerForEvent(
            @PathVariable("eventId") Long eventId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Get request for register for event");

        eventRegistrationService.registerForEvent(eventId, user);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel/{eventId}")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable("eventId") Long eventId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Get request for cancel registration");

        eventRegistrationService.cancelRegistration(eventId, user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<EventDto>> getMyRegistrations(
            @AuthenticationPrincipal User user
    ) {
        log.info("Get request for get my registrations");

        List<Event> events = eventRegistrationService.getEventsInWhichUserIsRegistered(user);

        List<EventDto> eventDtos = events
                .stream()
                .map(eventDtoMapper::toDto)
                .toList();

        return ResponseEntity.ok(eventDtos);
    }
}
