package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.enums.BookingFailureReason;
import com.github.theoydr.eventmanagement.enums.BookingStatus;
import com.github.theoydr.eventmanagement.enums.EventStatus;
import com.github.theoydr.eventmanagement.enums.UserRole;
import com.github.theoydr.eventmanagement.exception.EventBookingException;
import com.github.theoydr.eventmanagement.exception.OperationNotAllowedException;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.model.Booking;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.repository.BookingRepository;
import com.github.theoydr.eventmanagement.repository.EventRepository;
import com.github.theoydr.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    // --- Helper Methods ---

    private User createAttendee() {
        User user = new User();
        user.setId(2L);
        user.setRole(UserRole.USER);
        return user;
    }

    private User createOrganizer() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.ORGANIZER);
        return user;
    }

    private Event createPublishedEvent(User organizer) {
        Event event = new Event();
        event.setId(10L);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.PUBLISHED);
        event.setCapacity(100);
        event.setStartDateTime(LocalDateTime.now().plusDays(5)); // Future event
        return event;
    }

    // --- CREATE BOOKING TESTS ---

    @Test
    @DisplayName("Should create booking successfully when all rules are met")
    void createBooking_Success() {
        // Arrange
        Long userId = 2L;
        Long eventId = 10L;
        Integer tickets = 2;

        User attendee = createAttendee();
        User organizer = createOrganizer();
        Event event = createPublishedEvent(organizer);

        Booking savedBooking = new Booking(event, attendee, tickets, BookingStatus.CONFIRMED);
        savedBooking.setId(500L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Mock business rule checks
        when(bookingRepository.existsByUserAndEvent(attendee, event)).thenReturn(false);
        when(bookingRepository.findByEvent(event)).thenReturn(Collections.emptyList());

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Act
        Booking result = bookingService.createBooking(userId, eventId, tickets);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(500L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when User not found")
    void createBooking_UserNotFound_ThrowsException() {
        // Arrange

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(99L, 10L, 1))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(bookingRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should throw exception when Event not found")
    void createBooking_EventNotFound_ThrowsException() {
        // Arrange

        Long userId = 2L;

        User attendee = createAttendee();
        when(userRepository.findById(userId)).thenReturn(Optional.of(attendee));

        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(userId, 10L, 1))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(bookingRepository, never()).save(any());

    }


    @Test
    @DisplayName("Should fail if Organizer tries to book their own event")
    void createBooking_OrganizerSelfBooking_ThrowsException() {
        // Arrange
        User organizer = createOrganizer();
        Event event = createPublishedEvent(organizer);

        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, 1))
                .isInstanceOf(EventBookingException.class)
                .extracting("reasonCode") // Check the specific Enum failure reason
                .isEqualTo(BookingFailureReason.CANNOT_BOOK_OWN_EVENT);
        verify(bookingRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should fail if User has already booked the event")
    void createBooking_AlreadyBooked_ThrowsException() {
        // Arrange
        User attendee = createAttendee();
        Event event = createPublishedEvent(createOrganizer());

        when(userRepository.findById(attendee.getId())).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        // Simulate existing booking
        when(bookingRepository.existsByUserAndEvent(attendee, event)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(attendee.getId(), event.getId(), 1))
                .isInstanceOf(EventBookingException.class)
                .extracting("reasonCode")
                .isEqualTo(BookingFailureReason.USER_ALREADY_BOOKED);
        verify(bookingRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should fail if Event is not PUBLISHED (e.g. DRAFT or CANCELLED)")
    void createBooking_NotPublished_ThrowsException() {
        // Arrange
        User attendee = createAttendee();
        Event event = createPublishedEvent(createOrganizer());
        event.setStatus(EventStatus.DRAFT); // Wrong status

        when(userRepository.findById(attendee.getId())).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserAndEvent(attendee, event)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(attendee.getId(), event.getId(), 1))
                .isInstanceOf(EventBookingException.class)
                .extracting("reasonCode")
                .isEqualTo(BookingFailureReason.EVENT_NOT_PUBLISHED);
        verify(bookingRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should fail if Event has already started")
    void createBooking_EventStarted_ThrowsException() {
        // Arrange
        User attendee = createAttendee();
        Event event = createPublishedEvent(createOrganizer());
        event.setStartDateTime(LocalDateTime.now().minusHours(1)); // Started 1 hour ago

        when(userRepository.findById(attendee.getId())).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserAndEvent(attendee, event)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(attendee.getId(), event.getId(), 1))
                .isInstanceOf(EventBookingException.class)
                .extracting("reasonCode")
                .isEqualTo(BookingFailureReason.EVENT_IN_PAST);
        verify(bookingRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should fail if there is Insufficient Capacity")
    void createBooking_NoCapacity_ThrowsException() {
        // Arrange
        User attendee = createAttendee();
        Event event = createPublishedEvent(createOrganizer());
        event.setCapacity(10); // Capacity 10

        // Create a list of 10 existing bookings (1 ticket each)
        Booking b = new Booking();
        b.setNumberOfTickets(10);
        b.setStatus(BookingStatus.CONFIRMED);

        when(userRepository.findById(attendee.getId())).thenReturn(Optional.of(attendee));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserAndEvent(attendee, event)).thenReturn(false);
        // Simulate existing confirmed bookings
        when(bookingRepository.findByEvent(event)).thenReturn(List.of(b)); // 10 tickets sold

        // Act & Assert (Try to buy 1 more ticket)
        assertThatThrownBy(() -> bookingService.createBooking(attendee.getId(), event.getId(), 1))
                .isInstanceOf(EventBookingException.class)
                .extracting("reasonCode")
                .isEqualTo(BookingFailureReason.INSUFFICIENT_CAPACITY);
        verify(bookingRepository, never()).save(any());

    }


    // --- CANCEL BOOKING TEST ---

    @Test
    @DisplayName("Should cancel booking successfully")
    void cancelBooking_Success() {
        // Arrange
        Long bookingId = 500L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act
        bookingService.cancelBooking(bookingId);

        // Assert
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    @DisplayName("Should throw exception when Booking not found")
    void cancelBooking_NotFound_ThrowsException() {
        // Arrange
        Long bookingId = 500L;
        Booking booking = new Booking();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());


        //Act & Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never()).save(any());


    }

    @Test
    @DisplayName("Should throw exception if Booking already cancelled")
    void cancelBooking_AlreadyCancelled_ThrowsException() {
        // Arrange
        Long bookingId = 500L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));


        //Act & Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(OperationNotAllowedException.class);

        verify(bookingRepository, never()).save(any());


    }
}