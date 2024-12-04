package dev.vudovenko.eventmanagement.events.domain;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
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

    private Long ownerId;

    private Integer maxPlaces;

    private Integer occupiedPlaces;

    private LocalDateTime date;

    private Integer cost;

    private Integer duration;

    private Long locationId;

    private EventStatus status;
}