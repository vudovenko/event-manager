package dev.vudovenko.eventmanagement.events.entity;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "events", schema = "public", catalog = "postgres")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "max_places", nullable = false)
    private Integer maxPlaces;

    @Column(name = "occupied_places", nullable = false)
    private Integer occupiedPlaces;

    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime date;

    @Column(name = "cost", nullable = false)
    private Integer cost;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;
}
