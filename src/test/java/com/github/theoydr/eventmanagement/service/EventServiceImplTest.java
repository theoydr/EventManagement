package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.enums.EventCategory;
import com.github.theoydr.eventmanagement.enums.EventStatus;
import com.github.theoydr.eventmanagement.enums.UserRole;
import com.github.theoydr.eventmanagement.exception.DuplicateEventException;
import com.github.theoydr.eventmanagement.exception.OperationNotAllowedException;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.mapper.EventMapper;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.repository.EventRepository;
import com.github.theoydr.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    // --- Helpers to create dummy data ---
    private User createOrganizer() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.ORGANIZER);
        return user;
    }

    private EventRequest createEventRequest() {
        return new EventRequest(
                "Tech Conf", "Description", "Athens",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                100, 50.0, EventCategory.CONFERENCE, 1L
        );
    }

    // --- CREATE EVENT TESTS ---

    @Test
    @DisplayName("Should create event successfully when user is ORGANIZER and no duplicate exists")
    void createEvent_Success() {
        // Arrange
        EventRequest request = createEventRequest();
        User organizer = createOrganizer();
        Event mappedEvent = new Event(); // Empty event from mapper

        when(userRepository.findById(request.organizerId())).thenReturn(Optional.of(organizer));
        when(eventRepository.existsByOrganizerAndStartDateTimeAndLocation(any(), any(), any())).thenReturn(false);
        when(eventMapper.toEntity(request)).thenReturn(mappedEvent);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Event result = eventService.createEvent(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrganizer()).isEqualTo(organizer);
        assertThat(result.getStatus()).isEqualTo(EventStatus.DRAFT);
        verify(eventRepository).save(mappedEvent);
    }

    @Test
    @DisplayName("Should throw exception when Organizer not found")
    void createEvent_OrganizerNotFound_ThrowsException() {
        // Arrange
        EventRequest request = createEventRequest();
        when(userRepository.findById(request.organizerId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("organizer");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when User is not ORGANIZER")
    void createEvent_UserNotOrganizer_ThrowsException() {
        // Arrange
        EventRequest request = createEventRequest();
        User regularUser = new User();
        regularUser.setId(1L);
        regularUser.setRole(UserRole.USER); // Wrong role

        when(userRepository.findById(request.organizerId())).thenReturn(Optional.of(regularUser));

        // Act & Assert
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(OperationNotAllowedException.class)
                .hasMessageContaining("must have the ORGANIZER role");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when Duplicate Event exists")
    void createEvent_Duplicate_ThrowsException() {
        // Arrange
        EventRequest request = createEventRequest();
        User organizer = createOrganizer();

        when(userRepository.findById(request.organizerId())).thenReturn(Optional.of(organizer));
        when(eventRepository.existsByOrganizerAndStartDateTimeAndLocation(any(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(DuplicateEventException.class);

        verify(eventRepository, never()).save(any());
    }

    // --- UPDATE EVENT TESTS ---

    @Test
    @DisplayName("Should update event fields manually and save")
    void updateEvent_Success() {
        // Arrange
        Long eventId = 10L;
        EventRequest updateRequest = new EventRequest(
                "Updated Title", null, null, null, null, null, null, null, 1L
        );

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setTitle("Old Title");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        // Act
        Event result = eventService.updateEvent(eventId, updateRequest);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Updated Title"); // Changed
        verify(eventRepository).save(existingEvent);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent event")
    void updateEvent_NotFound_ThrowsException() {
        // Arrange
        Long eventId = 999L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> eventService.updateEvent(eventId, createEventRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any());

    }

    // --- PUBLISH EVENT TESTS ---

    @Test
    @DisplayName("Should publish event successfully")
    void publishEvent_Success() {
        // Arrange
        Long eventId = 10L;
        Long organizerId = 1L;

        User organizer = createOrganizer(); // ID=1, Role=ORGANIZER
        Event event = new Event();
        event.setId(eventId);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        // Act
        Event result = eventService.publishEvent(eventId, organizerId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("Should throw exception if requester is not the owner")
    void publishEvent_NotOwner_ThrowsException() {
        // Arrange
        Long eventId = 10L;
        Long wrongUserId = 999L;

        User organizer = createOrganizer(); // ID=1
        Event event = new Event();
        event.setId(eventId);
        event.setOrganizer(organizer);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThatThrownBy(() -> eventService.publishEvent(eventId, wrongUserId))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(eventRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should throw exception if event is not DRAFT")
    void publishEvent_NotDraft_ThrowsException() {
        // Arrange
        Long eventId = 10L;
        Long organizerId = 1L;

        User organizer = createOrganizer();
        Event event = new Event();
        event.setId(eventId);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.CANCELLED); // Wrong status

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThatThrownBy(() -> eventService.publishEvent(eventId, organizerId))
                .isInstanceOf(OperationNotAllowedException.class)
                .hasMessageContaining("Current status is");
        verify(eventRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should throw exception if owner was demoted from ORGANIZER")
    void publishEvent_OwnerDemoted_ThrowsException() {
        // Arrange
        Long eventId = 10L;
        Long organizerId = 1L;

        User demotedUser = new User();
        demotedUser.setId(1L);
        demotedUser.setRole(UserRole.USER); // Demoted

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizer(demotedUser);
        event.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThatThrownBy(() -> eventService.publishEvent(eventId, organizerId))
                .isInstanceOf(OperationNotAllowedException.class)
                .hasMessageContaining("must have the ORGANIZER role");
        verify(eventRepository, never()).save(any());

    }

    // --- CANCEL EVENT TESTS ---

    @Test
    @DisplayName("Should cancel event successfully")
    void cancelEvent_Success() {
        // Arrange
        Long eventId = 10L;
        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        eventService.cancelEvent(eventId);

        // Assert
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository).save(event);
    }


    @Test
    @DisplayName("Should throw exception when Event not found")
    void cancelEvent_NotFound_ThrowsException() {
        // Arrange
        Long eventId = 10L;
        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());


        //Act & Assert
        assertThatThrownBy(() -> eventService.cancelEvent(eventId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any());

    }

    @Test
    @DisplayName("Should throw exception if event already cancelled")
    void cancelEvent_AlreadyCancelled_ThrowsException() {
        // Arrange
        Long eventId = 10L;
        Event event = new Event();
        event.setId(eventId);
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));


        //Act & Assert
        assertThatThrownBy(() -> eventService.cancelEvent(eventId))
                .isInstanceOf(OperationNotAllowedException .class);
        verify(eventRepository, never()).save(any());

    }


}