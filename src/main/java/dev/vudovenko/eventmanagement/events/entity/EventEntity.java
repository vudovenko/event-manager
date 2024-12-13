package dev.vudovenko.eventmanagement.events.entity;

import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.entity.LocationEntity;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

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

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

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

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @ToString.Exclude
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private Set<EventRegistrationEntity> eventRegistrationEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;
}
