package dev.vudovenko.eventmanagement.events.services.impl;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.exceptions.DateEventInPastException;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationService locationService;

    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Event createEvent(Event event) {
        checkCorrectnessDate(event);
        checkAvailabilityPlaces(event);

        EventEntity createdEvent = eventRepository.save(
                modelMapper.map(event, EventEntity.class)
        );

        return modelMapper.map(createdEvent, Event.class);
    }

    private static void checkCorrectnessDate(Event event) {
        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new DateEventInPastException(event.getDate());
        }
    }

    private void checkAvailabilityPlaces(Event event) {
        Long locationId = event.getLocation().getId();
        Integer availablePlaces = locationService.getNumberAvailableSeats(locationId);

        if (event.getMaxPlaces() > availablePlaces) {
            throw new InsufficientSeatsException(availablePlaces);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    @Override
    public Event findById(Long id) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
