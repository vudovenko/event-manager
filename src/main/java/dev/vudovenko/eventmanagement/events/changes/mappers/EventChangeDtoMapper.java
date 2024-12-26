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
                new FieldChangeString(oldEvent.getName(), newEvent.getName()),
                new FieldChangeInteger(oldEvent.getMaxPlaces(), newEvent.getMaxPlaces()),
                new FieldChangeDateTime(oldEvent.getDate(), newEvent.getDate()),
                new FieldChangeInteger(oldEvent.getCost(), newEvent.getCost()),
                new FieldChangeInteger(oldEvent.getDuration(), newEvent.getDuration()),
                new FieldChangeLong(oldEvent.getLocation().getId(), newEvent.getLocation().getId()),
                new FieldChangeStatus(oldEvent.getStatus(), newEvent.getStatus()),
                participants
        );
    }

    public EventChangeDto toDto(
            Event eventWithNewStatus,
            EventStatus oldStatus,
            Long modifiedBy,
            List<Long> participants
    ) {
        return new EventChangeDto(
                eventWithNewStatus.getId(),
                modifiedBy,
                eventWithNewStatus.getOwner().getId(),
                new FieldChangeString(eventWithNewStatus.getName(), eventWithNewStatus.getName()),
                new FieldChangeInteger(eventWithNewStatus.getMaxPlaces(), eventWithNewStatus.getMaxPlaces()),
                new FieldChangeDateTime(eventWithNewStatus.getDate(), eventWithNewStatus.getDate()),
                new FieldChangeInteger(eventWithNewStatus.getCost(), eventWithNewStatus.getCost()),
                new FieldChangeInteger(eventWithNewStatus.getDuration(), eventWithNewStatus.getDuration()),
                new FieldChangeLong(eventWithNewStatus.getLocation().getId(), eventWithNewStatus.getLocation().getId()),
                new FieldChangeStatus(oldStatus, eventWithNewStatus.getStatus()),
                participants
        );
    }
}
