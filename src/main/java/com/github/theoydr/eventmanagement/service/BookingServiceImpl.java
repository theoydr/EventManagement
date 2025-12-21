package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.enums.BookingFailureReason;
import com.github.theoydr.eventmanagement.enums.BookingStatus;
import com.github.theoydr.eventmanagement.enums.EventStatus;
import com.github.theoydr.eventmanagement.exception.EventBookingException;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.model.Booking;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.repository.BookingRepository;
import com.github.theoydr.eventmanagement.repository.EventRepository;
import com.github.theoydr.eventmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);


    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Booking createBooking(Long userId, Long eventId, Integer numberOfTickets) {
        log.debug("Attempting to create booking for User ID: {} on Event ID: {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new  ResourceNotFoundException("event", "id", eventId));

        if (bookingRepository.existsByUserAndEvent(user, event)) {
            throw new EventBookingException(BookingFailureReason.USER_ALREADY_BOOKED, "You have already booked this event.");
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventBookingException(BookingFailureReason.EVENT_NOT_PUBLISHED, "Event is not published and cannot be booked.");
        }

        if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new EventBookingException(BookingFailureReason.EVENT_IN_PAST, "Cannot book an event that has already started.");
        }


        Integer confirmedBookings = bookingRepository.findByEvent(event).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .mapToInt(Booking::getNumberOfTickets)
                .sum();

        if (confirmedBookings + numberOfTickets > event.getCapacity()) {
            throw new EventBookingException(BookingFailureReason.INSUFFICIENT_CAPACITY, "Not enough tickets available for this event.");
        }

        Booking newBooking = new Booking(event, user, numberOfTickets, BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(newBooking);
        log.info("Booking created successfully. Booking ID: {}, User ID: {}, Event ID: {}", savedBooking.getId(), userId, eventId);

        return savedBooking;
    }

    @Override
    public void cancelBooking(Long bookingId) {
        log.debug("Attempting to cancel booking with ID: {}", bookingId);


        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("booking", "id", bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled successfully with ID: {}", bookingId);

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findBookingById(Long bookingId) {
        log.debug("Fetching booking by ID: {}", bookingId);
        return bookingRepository.findById(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByUser(Long userId) {
        log.debug("Fetching bookings for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));
        return bookingRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBookingsForEvent(Long eventId) {
        log.debug("Fetching bookings for event: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("event", "id", eventId));
        return bookingRepository.findByEvent(event);
    }

}
