package com.github.theoydr.eventmanagement.dto;

import com.github.theoydr.eventmanagement.enums.EventCategory;
import com.github.theoydr.eventmanagement.enums.EventStatus;

import java.time.LocalDateTime;

/**
 * Represents the publicly visible data for an event.
 */
public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer capacity,
        Double ticketPrice,
        EventCategory category,
        EventStatus status,
        OrganizerResponse organizer
) {}
