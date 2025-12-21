package com.github.theoydr.eventmanagement.enums;

public enum BookingFailureReason {
    /** The event has not been published and is not open for bookings. */
    EVENT_NOT_PUBLISHED,

    /** The event's start date is in the past. */
    EVENT_IN_PAST,

    /** The requested number of tickets exceeds the remaining capacity of the event. */
    INSUFFICIENT_CAPACITY,

    /** The user has already made a booking for this event. */
    USER_ALREADY_BOOKED,


}