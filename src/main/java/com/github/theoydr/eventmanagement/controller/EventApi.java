package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.dto.EventResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Events API", description = "Endpoints for managing events")
public interface EventApi {

    @Operation(summary = "Create a new event", description = "Creates a new event with the provided details. The event will be in DRAFT status initially.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Operation not allowed (e.g. role is not ORGANISER)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organizer not found with the provided ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A duplicate event (same organizer, start time, and location) already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<EventResponse> createEvent(@Parameter(description = "Details for the new event", required = true) @RequestBody EventRequest eventRequest);

    @Operation(summary = "Get an event by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the event",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<EventResponse> getEventById(@Parameter(description = "The ID of the event to retrieve", required = true) @PathVariable Long id);

    @Operation(summary = "Get all events", description = "Retrieves a list of all events.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of events")
    List<EventResponse> getAllEvents();

    @Operation(summary = "Update an existing event", description = "Updates the details of an existing event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<EventResponse> updateEvent(@Parameter(description = "The ID of the event to update", required = true) @PathVariable Long id,
                                              @Parameter(description = "The updated event details", required = true) @RequestBody EventRequest eventRequest);

    @Operation(summary = "Cancel an event", description = "Changes the status of an event to CANCELLED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event cancelled successfully (No content)"),
            @ApiResponse(responseCode = "403", description = "Operation not allowed (e.g. event is already CANCELLED)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<Void> cancelEvent(@Parameter(description = "The ID of the event to cancel", required = true) @PathVariable Long id);


    @Operation(summary = "Publish an event", description = "Changes the status of a DRAFT event to PUBLISHED, making it available for booking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event published successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "403", description = "Operation not allowed (e.g., Event is not in DRAFT status, role is not ORGANISER)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found with the given ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<EventResponse> publishEvent(@Parameter(description = "The ID of the event to publish", required = true) @PathVariable Long id,
                                               @Parameter(description = "The ID of the organizer publishing the event", required = true) @RequestParam Long organizerId);

    @Operation(summary = "Get all published events", description = "Retrieves a list of all events with status PUBLISHED.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of published events")
    List<EventResponse> getPublishedEvents();

}
