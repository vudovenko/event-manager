package dev.vudovenko.eventmanagement.events.changes.dto;

public record FieldChange<T>(

        T oldField,
        T newField
) {
}
