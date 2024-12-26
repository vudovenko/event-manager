package dev.vudovenko.eventmanagement.events.changes.dto;

import java.util.List;

public record EventChangeDto(

        Long eventId,
        Long modifiedBy,
        Long owner,
        FieldChangeString name,
        FieldChangeInteger maxPlaces,
        FieldChangeDateTime date,
        FieldChangeInteger cost,
        FieldChangeInteger duration,
        FieldChangeLong locationId,
        FieldChangeStatus status,
        List<Long> participants
) {
}
