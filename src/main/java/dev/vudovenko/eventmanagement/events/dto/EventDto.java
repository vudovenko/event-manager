package dev.vudovenko.eventmanagement.events.dto;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

public record EventDto(

        Long id,

        String name,

        Long ownerId,

        Integer maxPlaces,

        Integer occupiedPlaces,

        String date,

        Integer cost,

        Integer duration,

        Long locationId,

        EventStatus status
) {
}