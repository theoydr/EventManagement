package com.github.theoydr.eventmanagement.dto;

import com.github.theoydr.eventmanagement.enums.BookingStatus;

import java.time.LocalDateTime;


/**
 * Represents the publicly visible data for a booking.
 * It includes nested DTOs for the associated user and event.
 */
public record BookingResponse(
        Long id,
        Integer numberOfTickets,
        LocalDateTime bookingDateTime,
        BookingStatus status,
        EventResponse event,
        AttendeeResponse user
) {}
