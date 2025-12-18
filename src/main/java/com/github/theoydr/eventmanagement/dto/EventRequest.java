package com.github.theoydr.eventmanagement.dto;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import com.github.theoydr.eventmanagement.enums.EventCategory;
import com.github.theoydr.eventmanagement.validator.ValidEventDates;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Represents the data required to create or update an event.
 */
@ValidEventDates(message = MessageKeys.EventMessages.DATES_VALID)
public record EventRequest(
        @NotBlank(message = MessageKeys.EventMessages.TITLE_NOT_BLANK)
        @Size(min = 3, max = 100, message = MessageKeys.EventMessages.TITLE_SIZE)
        String title,

        @NotBlank(message = MessageKeys.EventMessages.DESCRIPTION_NOT_BLANK)
        @Size(max = 2000, message = MessageKeys.EventMessages.DESCRIPTION_SIZE)
        String description,

        @NotBlank(message = MessageKeys.EventMessages.LOCATION_NOT_BLANK)
        String location,

        @NotNull(message = MessageKeys.EventMessages.START_DATE_NOT_NULL)
        @Future(message = MessageKeys.EventMessages.START_DATE_FUTURE)
        LocalDateTime startDateTime,

        @NotNull(message = MessageKeys.EventMessages.END_DATE_NOT_NULL)
        @Future(message = MessageKeys.EventMessages.END_DATE_FUTURE)
        LocalDateTime endDateTime,

        @NotNull(message = MessageKeys.EventMessages.CAPACITY_NOT_NULL)
        @Min(value = 1, message = MessageKeys.EventMessages.CAPACITY_MIN)
        Integer capacity,

        @NotNull(message = MessageKeys.EventMessages.PRICE_NOT_NULL)
        @DecimalMin(value = "0.0", message = MessageKeys.EventMessages.PRICE_MIN)
        Double ticketPrice,

        @NotNull(message = MessageKeys.EventMessages.CATEGORY_NOT_NULL)
        EventCategory category,

        @NotNull(message = MessageKeys.EventMessages.ORGANIZER_NOT_NULL)
        Long organizerId
) {}

