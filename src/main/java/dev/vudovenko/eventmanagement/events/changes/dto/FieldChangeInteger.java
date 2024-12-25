package dev.vudovenko.eventmanagement.events.changes.dto;

public record FieldChangeInteger(

        Integer oldField,
        Integer newField
) {
}
