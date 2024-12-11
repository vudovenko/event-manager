package dev.vudovenko.eventmanagement.eventRegistrations.controllers;

import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
