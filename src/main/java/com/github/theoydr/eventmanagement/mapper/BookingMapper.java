package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.BookingResponse;
import com.github.theoydr.eventmanagement.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public BookingMapper(EventMapper eventMapper, UserMapper userMapper) {
        this.eventMapper = eventMapper;
        this.userMapper = userMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getNumberOfTickets(),
                booking.getBookingDateTime(),
                booking.getStatus(),
                eventMapper.toResponse(booking.getEvent()),
                userMapper.toResponse(booking.getUser())
        );
    }
}
