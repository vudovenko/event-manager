package dev.vudovenko.eventmanagement.utils.dataProviders;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

import java.util.stream.Stream;

public class EventTestDataProviders {

    public static Stream<EventStatus> eventStatusesValidForRegisterProvider() {
        return Stream.of(
                EventStatus.WAIT_START,
                EventStatus.STARTED
        );
    }

    public static Stream<EventStatus> eventStatusesNotValidForRegisterProvider() {
        return Stream.of(
                EventStatus.CANCELLED,
                EventStatus.FINISHED
        );
    }
}
