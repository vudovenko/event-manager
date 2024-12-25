package dev.vudovenko.eventmanagement.events.changes.dto;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

public record FieldChangeStatus(

        EventStatus oldField,
        EventStatus newField
) {
}
