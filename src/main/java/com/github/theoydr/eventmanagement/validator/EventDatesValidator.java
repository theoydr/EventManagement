package com.github.theoydr.eventmanagement.validator;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventDatesValidator implements ConstraintValidator<ValidEventDates, EventRequest> {

    @Override
    public void initialize(ValidEventDates constraintAnnotation) {
    }

    @Override
    public boolean isValid(EventRequest eventRequest, ConstraintValidatorContext context) {
        if (eventRequest.startDateTime() == null || eventRequest.endDateTime() == null) {
            // Let the @NotNull annotation on the fields handle this case.
            // This validator is only concerned with the relationship between the two dates.
            return true;
        }
        return eventRequest.endDateTime().isAfter(eventRequest.startDateTime());
    }
}

