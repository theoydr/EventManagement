package com.github.theoydr.eventmanagement.service;


import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.enums.EventStatus;
import com.github.theoydr.eventmanagement.exception.DuplicateEventException;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.mapper.EventMapper;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.repository.EventRepository;
import com.github.theoydr.eventmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    public Event createEvent(EventRequest eventRequest) {
        log.debug("Attempting to create new event with title: {}", eventRequest.title());

        User organizer = userRepository.findById(eventRequest.organizerId())
                .orElseThrow(() -> new ResourceNotFoundException("organizer", "organizerId", eventRequest.organizerId()));

        if (eventRepository.existsByOrganizerAndStartDateTimeAndLocation(organizer, eventRequest.startDateTime(), eventRequest.location())) {
            throw new DuplicateEventException();
        }
        Event event = eventMapper.toEntity(eventRequest);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);
        Event savedEvent = eventRepository.save(event);
        log.info("New event created successfully with ID: {} and Title: {}", savedEvent.getId(), savedEvent.getTitle());

        return savedEvent;
    }

    @Override
    public Event updateEvent(Long eventId, EventRequest eventRequest) {
        log.debug("Attempting to update event with ID: {}", eventId);
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("event", "id", eventId));

        // Update fields from the request
        Optional.ofNullable(eventRequest.title()).ifPresent(existingEvent::setTitle);
        Optional.ofNullable(eventRequest.description()).ifPresent(existingEvent::setDescription);
        Optional.ofNullable(eventRequest.location()).ifPresent(existingEvent::setLocation);
        Optional.ofNullable(eventRequest.startDateTime()).ifPresent(existingEvent::setStartDateTime);
        Optional.ofNullable(eventRequest.endDateTime()).ifPresent(existingEvent::setEndDateTime);
        Optional.ofNullable(eventRequest.capacity()).ifPresent(existingEvent::setCapacity);
        Optional.ofNullable(eventRequest.ticketPrice()).ifPresent(existingEvent::setTicketPrice);
        Optional.ofNullable(eventRequest.category()).ifPresent(existingEvent::setCategory);
        Event updatedEvent = eventRepository.save(existingEvent);
        log.info("Event updated successfully with ID: {}", updatedEvent.getId());

        return updatedEvent;
    }

    @Override
    public void cancelEvent(Long eventId) {
        log.debug("Attempting to cancel event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("event", "id", eventId));
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Event cancelled successfully with ID: {}", eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findEventById(Long eventId) {
        log.debug("Fetching event by ID: {}", eventId);
        return eventRepository.findById(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAllEvents() {
        log.debug("Fetching all events");
        return eventRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findPublishedEvents() {
        log.debug("Fetching all published events");
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }
}
