package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.exception.EventBookingException;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.model.Booking;

import java.util.List;
import java.util.Optional;


/**
 * Service interface for managing bookings.
 * Defines the contract for eventmanagement-related business operations.
 */
public interface BookingService {



    /**
     * Creates a new eventmanagement for a user for a specific event.
     *
     * @param userId The ID of the user making the eventmanagement.
     * @param eventId The ID of the event being booked.
     * @param numberOfTickets The number of tickets to book.
     * @return The newly created and persisted Booking entity.
     * @throws ResourceNotFoundException if the user or event is not found.
     * @throws EventBookingException if the eventmanagement violates any business rules (e.g., event not published, sold out).
     */
    Booking createBooking(Long userId, Long eventId, Integer numberOfTickets);


    /**
     * Cancels a eventmanagement by changing its status.
     *
     * @param bookingId The ID of the eventmanagement to cancel.
     * @throws ResourceNotFoundException if no eventmanagement is found with the given ID.
     */
    void cancelBooking(Long bookingId);


    /**
     * Finds a eventmanagement by its unique ID.
     *
     * @param bookingId The ID of the eventmanagement to find.
     * @return an {@link Optional} containing the found eventmanagement, or {@link Optional#empty()} if no eventmanagement is found.
     */
    Optional<Booking> findBookingById(Long bookingId);

    /**
     * Finds all bookings for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of bookings made by the user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    List<Booking> findBookingsByUser(Long userId);


    /**
     * Finds all bookings for a specific event.
     *
     * @param eventId The ID of the event.
     * @return A list of bookings belonging to the event.
     * @throws ResourceNotFoundException if the event is not found.
     */
    List<Booking> findBookingsForEvent(Long eventId);
}
