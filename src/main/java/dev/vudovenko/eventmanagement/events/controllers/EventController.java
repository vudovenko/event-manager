package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final DtoMapper<Event, EventDto> eventDtoMapper;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
            @Valid @RequestBody EventCreateRequestDto eventCreateRequestDto
    ) {
        log.info("Get request for create event");

        Event createdEvent = eventService.createEvent(
                requestEventToDomain(eventCreateRequestDto)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventDtoMapper.toDto(createdEvent));
    }

    private Event requestEventToDomain(EventCreateRequestDto eventCreateRequestDto) {
        return new Event(
                null,
                eventCreateRequestDto.name(),
                null,
                eventCreateRequestDto.maxPlaces(),
                null,
                eventCreateRequestDto.date(),
                eventCreateRequestDto.cost(),
                eventCreateRequestDto.duration(),
                eventCreateRequestDto.locationId(),
                null
        );
    }
}
