package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.users.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventRegistrationTestUtils {

    @Autowired
    private EventRegistrationService eventRegistrationService;
    @Autowired
    private UserTestUtils UserTestUtils;
    @Autowired
    private EventTestUtils eventTestUtils;

    public void createEventRegistration() {
        User user = UserTestUtils.getRegisteredUser();
        Event event = eventTestUtils.getCreatedEvent();

        eventRegistrationService.registerForEvent(
                event.getId(),
                user
        );
    }
}
