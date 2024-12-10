package dev.vudovenko.eventmanagement.events.dto;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record EventSearchRequestDto(

        String name,

        @Min(0)
        Integer placesMin,

        @Min(0)
        Integer placesMax,

        LocalDateTime dateStartAfter,

        LocalDateTime dateStartBefore,

        @Min(0)
        Double costMin,

        @Min(0)
        Double costMax,

        @Min(30)
        Integer durationMin,

        @Min(30)
        Integer durationMax,

        Integer locationId,

        EventStatus eventStatus
) {
}