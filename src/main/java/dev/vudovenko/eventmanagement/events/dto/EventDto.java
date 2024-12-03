package dev.vudovenko.eventmanagement.events.dto;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

import java.time.LocalDateTime;

public record EventDto(

        Long id,

        String name,

        Long ownerId,

        Integer maxPlaces,

        Integer occupiedPlaces,

        LocalDateTime date,

        Integer cost,

        Integer duration,

        Long locationId,

        EventStatus status
) {
}