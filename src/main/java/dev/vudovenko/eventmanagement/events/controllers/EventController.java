package dev.vudovenko.eventmanagement.events.controllers;

import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.common.mappers.ToDomainMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventDto;
import dev.vudovenko.eventmanagement.events.dto.EventSearchRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventUpdateRequestDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final ToDomainMapper<Event, EventCreateRequestDto> createRequestDtoMapper;
    private final ToDomainMapper<Event, EventUpdateRequestDto> updateRequestDtoMapper;
    private final DtoMapper<Event, EventDto> eventDtoMapper;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
            @Valid @RequestBody EventCreateRequestDto eventCreateRequestDto
    ) {
        log.info("Get request for create event");

        Event createdEvent = eventService.createEvent(
                createRequestDtoMapper.toDomain(eventCreateRequestDto)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventDtoMapper.toDto(createdEvent));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @NotNull @PathVariable("eventId") Long eventId
    ) {
        log.info("Get request for delete event");

        eventService.deleteEvent(eventId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEvent(
            @PathVariable("eventId") Long eventId
    ) {
        log.info("Get request for get event");

        Event event = eventService.findById(eventId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventDtoMapper.toDto(event));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody EventUpdateRequestDto eventUpdateRequestDto
    ) {
        log.info("Get request for update event");

        Event event = eventService.updateEvent(
                eventId,
                updateRequestDtoMapper.toDomain(eventUpdateRequestDto)
        );

        return ResponseEntity.ok(eventDtoMapper.toDto(event));
    }

    @PostMapping("/search")
    public ResponseEntity<List<EventDto>> searchEvents(
            @Valid @RequestBody EventSearchRequestDto eventSearchRequestDto
    ) {
        log.info("Get request for search events");

        List<Event> events = eventService.searchEvents(
                eventSearchRequestDto.name(),
                eventSearchRequestDto.placesMin(),
                eventSearchRequestDto.placesMax(),
                eventSearchRequestDto.dateStartAfter(),
                eventSearchRequestDto.dateStartBefore(),
                eventSearchRequestDto.costMin(),
                eventSearchRequestDto.costMax(),
                eventSearchRequestDto.durationMin(),
                eventSearchRequestDto.durationMax(),
                eventSearchRequestDto.locationId(),
                eventSearchRequestDto.eventStatus()
        );

        List<EventDto> eventDtos = events
                .stream()
                .map(eventDtoMapper::toDto)
                .toList();

        return ResponseEntity.ok(eventDtos);
    }
}
