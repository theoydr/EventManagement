package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.dto.BookingRequest;
import com.github.theoydr.eventmanagement.dto.BookingResponse;
import com.github.theoydr.eventmanagement.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Bookings API", description = "Endpoints for managing event bookings")
public interface BookingApi {

    @Operation(summary = "Create a new booking", description = "Creates a booking for a user for a specific event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., negative tickets)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or Event not found with the given IDs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Booking failed due to a business rule (e.g., event sold out, user already booked)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<BookingResponse> createBooking(@Parameter(description = "Details for the new booking", required = true) @RequestBody BookingRequest bookingRequest);

    @Operation(summary = "Cancel a booking", description = "Changes the status of a booking to CANCELLED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking cancelled successfully (No content)"),
            @ApiResponse(responseCode = "403", description = "Operation not allowed (e.g. booking is already CANCELLED)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<Void> cancelBooking(@PathVariable Long id);

    @Operation(summary = "Get a booking by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the booking",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<BookingResponse> getBookingById(@Parameter(description = "The ID of the booking to retrieve", required = true) @PathVariable Long id);

    @Operation(summary = "Get all bookings for a specific event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of bookings"),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    List<BookingResponse> getBookingsForEvent(@PathVariable Long eventId);


    @Operation(summary = "Get all bookings for a specific user", description = "Retrieves a list of all bookings made by a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of bookings"),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    List<BookingResponse> getBookingsByUser(@Parameter(description = "The ID of the user whose bookings to retrieve", required = true) @PathVariable Long userId);
}

