package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.dto.BookingRequest;
import com.github.theoydr.eventmanagement.dto.BookingResponse;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.mapper.BookingMapper;
import com.github.theoydr.eventmanagement.model.Booking;
import com.github.theoydr.eventmanagement.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController implements BookingApi {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        Booking newBooking = bookingService.createBooking(
                bookingRequest.userId(),
                bookingRequest.eventId(),
                bookingRequest.numberOfTickets()
        );
        return new ResponseEntity<>(bookingMapper.toResponse(newBooking), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.findBookingById(id)
                .orElseThrow(() -> new ResourceNotFoundException("booking", "id", id));
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public List<BookingResponse> getBookingsByUser(@PathVariable Long userId) {
        return bookingService.findBookingsByUser(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/event/{eventId}")
    public List<BookingResponse> getBookingsForEvent(@PathVariable Long eventId) {
        return bookingService.findBookingsForEvent(eventId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}
