package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.AttendeeResponse;
import com.github.theoydr.eventmanagement.dto.BookingResponse;
import com.github.theoydr.eventmanagement.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final EventMapper eventMapper;

    public BookingMapper(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        AttendeeResponse attendee = new AttendeeResponse(
                booking.getUser().getId(),
                booking.getUser().getUsername(),
                booking.getUser().getEmail()
        );

        return new BookingResponse(
                booking.getId(),
                booking.getNumberOfTickets(),
                booking.getBookingDateTime(),
                booking.getStatus(),
                eventMapper.toResponse(booking.getEvent()),
                attendee
        );
    }
}
