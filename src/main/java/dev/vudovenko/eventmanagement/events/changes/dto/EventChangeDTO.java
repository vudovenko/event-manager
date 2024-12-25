package dev.vudovenko.eventmanagement.events.changes.dto;

import java.util.List;

public record EventChangeDTO(

        Long eventId,
        Long modifiedBy,
        Long owner,
        FieldChangeString name,
        FieldChangeInteger maxPlaces,
        FieldChangeDateTime date,
        FieldChangeInteger cost,
        FieldChangeInteger duration,
        FieldChangeInteger locationId,
        FieldChangeStatus status,
        List<Integer> participants
) {
}
