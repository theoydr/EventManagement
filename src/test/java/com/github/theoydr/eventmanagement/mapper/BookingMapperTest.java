package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.BookingResponse;
import com.github.theoydr.eventmanagement.dto.EventResponse;
import com.github.theoydr.eventmanagement.enums.BookingStatus;
import com.github.theoydr.eventmanagement.model.Booking;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private BookingMapper bookingMapper;

    @Test
    @DisplayName("Should map Booking entity to BookingResponse DTO correctly")
    void toResponse_MapsFieldsCorrectly() {
        // Arrange
        // 1. Setup User (Attendee)
        User user = new User();
        user.setId(5L);
        user.setUsername("attendeeUser");
        user.setEmail("attendee@example.com");

        // 2. Setup Event
        Event event = new Event();
        event.setId(10L);
        event.setTitle("Awesome Concert");

        // 3. Setup Booking
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setNumberOfTickets(2);
        LocalDateTime bookingDate = LocalDateTime.now();
        booking.setBookingDateTime(bookingDate);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUser(user);
        booking.setEvent(event);

        EventResponse mockEventResponse = mock(EventResponse.class);
        when(eventMapper.toResponse(event)).thenReturn(mockEventResponse);

        // Act
        BookingResponse response = bookingMapper.toResponse(booking);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.numberOfTickets()).isEqualTo(2);
        assertThat(response.bookingDateTime()).isEqualTo(bookingDate);
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);

        // Verify nested AttendeeResponse mapping
        assertThat(response.user()).isNotNull();
        assertThat(response.user().id()).isEqualTo(5L);
        assertThat(response.user().name()).isEqualTo("attendeeUser");
        assertThat(response.user().email()).isEqualTo("attendee@example.com");

        // Verify EventResponse is passed through from the mocked mapper
        assertThat(response.event()).isEqualTo(mockEventResponse);
    }
}