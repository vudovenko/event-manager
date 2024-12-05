package dev.vudovenko.eventmanagement.locations.domain;

import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private String description;

    @ToString.Exclude
    private Set<EventEntity> events;
}
