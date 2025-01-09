package dev.vudovenko.eventmanagement.events.changes.dto;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EventChangeDto(

        Long eventId,
        Long modifiedBy,
        Long ownerId,
        FieldChange<String> name,
        FieldChange<Integer> maxPlaces,
        FieldChange<LocalDateTime> date,
        FieldChange<Integer> cost,
        FieldChange<Integer> duration,
        FieldChange<Long> locationId,
        FieldChange<EventStatus> status,
        List<Long> participants
) {
}
