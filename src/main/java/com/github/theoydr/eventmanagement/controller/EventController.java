package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.dto.EventResponse;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.mapper.EventMapper;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController implements EventApi {

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @Override
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        Event createdEvent = eventService.createEvent(eventRequest);
        return new ResponseEntity<>(eventMapper.toResponse(createdEvent), HttpStatus.CREATED);
    }


    @Override
    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.findAllEvents().stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    @GetMapping("/published")
    public List<EventResponse> getPublishedEvents() {
        return eventService.findPublishedEvents().stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        Event event = eventService.findEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("event", "id", id));
        return ResponseEntity.ok(eventMapper.toResponse(event));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest eventRequest) {
        Event updatedEvent = eventService.updateEvent(id, eventRequest);
        return ResponseEntity.ok(eventMapper.toResponse(updatedEvent));
    }

    @Override
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id, @RequestParam Long organizerId) {
        Event publishedEvent = eventService.publishEvent(id, organizerId);
        return ResponseEntity.ok(eventMapper.toResponse(publishedEvent));
    }
}
