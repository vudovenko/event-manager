package dev.vudovenko.eventmanagement.eventRegistrations.domain;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    private Long id;

    private User user;

    private Event event;
}
