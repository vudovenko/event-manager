package dev.vudovenko.eventmanagement.eventRegistrations.controllers;

import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/events/registrations")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

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
}
