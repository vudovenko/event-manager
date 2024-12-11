package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.events.dto.EventSearchRequestDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventSearchRequestDtoTestUtils {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserTestUtils userTestUtils;
    @Autowired
    private LocationTestUtils locationTestUtils;

    public EventSearchRequestDto getBlankEventSearchRequestDto() {
        return new EventSearchRequestDto(
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public EventSearchRequestDto getNotValidEventCreateRequestDto() {
        return new EventSearchRequestDto(
                null,
                -Math.abs(RandomUtils.getRandomInt()),
                -Math.abs(RandomUtils.getRandomInt()),
                null,
                null,
                -Math.abs(RandomUtils.getRandomInt()),
                -Math.abs(RandomUtils.getRandomInt()),
                -Math.abs(RandomUtils.getRandomInt()),
                -Math.abs(RandomUtils.getRandomInt()),
                null,
                null
        );
    }
}
