package com.github.theoydr.eventmanagement.dto;


import com.github.theoydr.eventmanagement.constants.MessageKeys;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


/**
 * Represents the data required to create a new booking.
 * This record is a wrapper for the @RequestBody in the BookingController.
 */
public record BookingRequest(
        @NotNull(message = MessageKeys.BookingMessages.USER_ID_REQUIRED)
        Long userId,

        @NotNull(message = MessageKeys.BookingMessages.EVENT_ID_REQUIRED)
        Long eventId,

        @NotNull(message = MessageKeys.BookingMessages.TICKETS_NOT_ZERO)
        @Min(value = 1, message = MessageKeys.BookingMessages.TICKETS_MIN)
        Integer numberOfTickets
) {}
