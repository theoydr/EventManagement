package com.github.theoydr.eventmanagement.exception;

import com.github.theoydr.eventmanagement.enums.BookingFailureReason;

import java.util.Map;

/**
 * Custom exception for errors related to event logic, such as an event being full,
 * already cancelled, or not in a state that allows a specific action.
 *
 * Responds with HTTP 409 Conflict.
 */
public class EventBookingException extends RuntimeException implements StructuredError{

    private final BookingFailureReason reasonCode;

    public EventBookingException(BookingFailureReason reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    /**
     * Returns the structured error arguments, including the specific reason code,
     * for building a detailed API error response.
     * @return A map of the error details.
     */
    @Override
    public Map<String, Object> getArguments() {
        return Map.of("reasonCode", reasonCode.toString());
    }

    public BookingFailureReason getReasonCode() {
        return reasonCode;
    }
}
