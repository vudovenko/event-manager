package dev.vudovenko.eventmanagement.events.services.impl;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    @Override
    public Event createEvent(Event event) {
        throw new UnsupportedOperationException("not implemented yet");

//        EventEntity createdEvent = eventRepository.save(
//                modelMapper.map(event, EventEntity.class)
//        );
//
//        return modelMapper.map(createdEvent, Event.class);
    }
}
