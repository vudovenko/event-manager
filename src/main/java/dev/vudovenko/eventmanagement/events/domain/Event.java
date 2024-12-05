package dev.vudovenko.eventmanagement.events.domain;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private Long id;

    private String name;

    private User owner;

    private Integer maxPlaces;

    private Integer occupiedPlaces;

    private LocalDateTime date;

    private Integer cost;

    private Integer duration;

    private Location location;

    private EventStatus status;
}
