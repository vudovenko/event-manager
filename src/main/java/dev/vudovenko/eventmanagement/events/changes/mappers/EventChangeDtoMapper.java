package dev.vudovenko.eventmanagement.events.changes.mappers;

import dev.vudovenko.eventmanagement.events.changes.dto.*;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventChangeDtoMapper {

    public EventChangeDto toDto(Event oldEvent, Event newEvent, Long modifiedBy, List<Long> participants) {
        return new EventChangeDto(
                oldEvent.getId(),
                modifiedBy,
                newEvent.getOwner().getId(),
                new FieldChange<>(oldEvent.getName(), newEvent.getName()),
                new FieldChange<>(oldEvent.getMaxPlaces(), newEvent.getMaxPlaces()),
                new FieldChange<>(oldEvent.getDate(), newEvent.getDate()),
                new FieldChange<>(oldEvent.getCost(), newEvent.getCost()),
                new FieldChange<>(oldEvent.getDuration(), newEvent.getDuration()),
                new FieldChange<>(oldEvent.getLocation().getId(), newEvent.getLocation().getId()),
                new FieldChange<>(oldEvent.getStatus(), newEvent.getStatus()),
                participants
        );
    }

    public EventChangeDto toDto(
            Event eventWithNewStatus,
            EventStatus oldStatus,
            List<Long> participants
    ) {
        return new EventChangeDto(
                eventWithNewStatus.getId(),
                null,
                eventWithNewStatus.getOwner().getId(),
                new FieldChange<>(eventWithNewStatus.getName(), eventWithNewStatus.getName()),
                new FieldChange<>(eventWithNewStatus.getMaxPlaces(), eventWithNewStatus.getMaxPlaces()),
                new FieldChange<>(eventWithNewStatus.getDate(), eventWithNewStatus.getDate()),
                new FieldChange<>(eventWithNewStatus.getCost(), eventWithNewStatus.getCost()),
                new FieldChange<>(eventWithNewStatus.getDuration(), eventWithNewStatus.getDuration()),
                new FieldChange<>(eventWithNewStatus.getLocation().getId(), eventWithNewStatus.getLocation().getId()),
                new FieldChange<>(oldStatus, eventWithNewStatus.getStatus()),
                participants
        );
    }
}
