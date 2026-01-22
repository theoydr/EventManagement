package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.dto.EventResponse;
import com.github.theoydr.eventmanagement.enums.EventCategory;
import com.github.theoydr.eventmanagement.enums.EventStatus;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final EventMapper eventMapper = new EventMapper();

    @Test
    @DisplayName("Should map EventRequest to Event entity correctly")
    void toEntity_MapsFieldsCorrectly() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        EventRequest request = new EventRequest(
                "Tech Conf 2025",
                "A deep dive into Java",
                "Athens Convention Center",
                start,
                end,
                500,
                150.00,
                EventCategory.CONFERENCE,
                1L // Organizer ID (not used in this specific toEntity method, but part of DTO)
        );

        // Act
        Event event = eventMapper.toEntity(request);

        // Assert
        assertThat(event).isNotNull();
        assertThat(event.getTitle()).isEqualTo("Tech Conf 2025");
        assertThat(event.getDescription()).isEqualTo("A deep dive into Java");
        assertThat(event.getLocation()).isEqualTo("Athens Convention Center");
        assertThat(event.getStartDateTime()).isEqualTo(start);
        assertThat(event.getEndDateTime()).isEqualTo(end);
        assertThat(event.getCapacity()).isEqualTo(500);
        assertThat(event.getTicketPrice()).isEqualTo(150.00);
        assertThat(event.getCategory()).isEqualTo(EventCategory.CONFERENCE);

        // Note: The provided toEntity does not set the Organizer or Status, so they should be null/default
        assertThat(event.getOrganizer()).isNull();
        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @DisplayName("Should map Event entity to EventResponse DTO with Organizer details")
    void toResponse_MapsFieldsAndOrganizer() {
        // Arrange
        User organizer = new User();
        organizer.setId(99L);
        organizer.setUsername("CoolOrganizer");
        organizer.setEmail("private@email.com"); // Should NOT appear in response

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(5);

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Music Festival");
        event.setDescription("Live bands");
        event.setLocation("Park");
        event.setStartDateTime(start);
        event.setEndDateTime(end);
        event.setCapacity(1000);
        event.setTicketPrice(50.0);
        event.setCategory(EventCategory.FESTIVAL);
        event.setStatus(EventStatus.PUBLISHED);
        event.setOrganizer(organizer);

        // Act
        EventResponse response = eventMapper.toResponse(event);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Music Festival");
        assertThat(response.description()).isEqualTo("Live bands");
        assertThat(response.location()).isEqualTo("Park");

        // --- ADDED MISSING ASSERTIONS ---
        assertThat(response.startDateTime()).isEqualTo(start);
        assertThat(response.endDateTime()).isEqualTo(end);
        assertThat(response.capacity()).isEqualTo(1000);
        assertThat(response.ticketPrice()).isEqualTo(50.0);
        assertThat(response.category()).isEqualTo(EventCategory.FESTIVAL);
         assertThat(response.status()).isEqualTo(EventStatus.PUBLISHED);

        // Check nested OrganizerResponse
        assertThat(response.organizer()).isNotNull();
        assertThat(response.organizer().id()).isEqualTo(99L);
        assertThat(response.organizer().name()).isEqualTo("CoolOrganizer");
    }
}