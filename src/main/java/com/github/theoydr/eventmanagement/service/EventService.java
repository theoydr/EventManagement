package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.exception.DuplicateEventException;
import com.github.theoydr.eventmanagement.exception.OperationNotAllowedException;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing events.
 * Defines the contract for event-related business operations.
 */
public interface EventService {


    /**
     * Creates a new event.
     *
     * @param eventRequest DTO containing the details for the new event.
     * @return The newly created and persisted Event entity.
     * @throws ResourceNotFoundException if the specified organizer is not found.
     * @throws DuplicateEventException if a similar event already exists.
     */
    Event createEvent(EventRequest eventRequest);


    /**
     * Updates an existing event's details.
     *
     * @param eventId The ID of the event to update.
     * @param eventRequest DTO containing the updated event details.
     * @return The updated and persisted Event entity.
     * @throws ResourceNotFoundException if no event is found with the given ID.
     */
    Event updateEvent(Long eventId, EventRequest eventRequest);


    /**
     * Cancels an event by changing its status.
     *
     * @param eventId The ID of the event to cancel.
     * @throws ResourceNotFoundException if no event is found with the given ID.
     */
    void cancelEvent(Long eventId);

    /**
     * Publishes a DRAFT event, making it available for bookings.
     *
     * @param eventId The ID of the event to publish.
     * @param organizerId The ID of the user attempting to publish the event.
     * @return The updated Event entity.
     * @throws ResourceNotFoundException if no event is found with the given ID.
     * @throws OperationNotAllowedException if the event is not in DRAFT status.
     */
    Event publishEvent(Long eventId, Long organizerId);

    /**
     * Finds an event by its unique ID.
     *
     * @param eventId The ID of the event to find.
     * @return an {@link Optional} containing the found event, or {@link Optional#empty()} if no event is found.
     */
    Optional<Event> findEventById(Long eventId);


    /**
     * Retrieves a list of all events.
     *
     * @return A list of all Event entities.
     */
    List<Event> findAllEvents();


    /**
     * Retrieves a list of events with PUBLISHED status.
     */
    List<Event> findPublishedEvents();
}