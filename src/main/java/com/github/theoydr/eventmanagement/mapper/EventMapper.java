package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.EventRequest;
import com.github.theoydr.eventmanagement.dto.EventResponse;
import com.github.theoydr.eventmanagement.dto.OrganizerResponse;
import com.github.theoydr.eventmanagement.model.Event;
import org.springframework.stereotype.Component;


@Component
public class EventMapper {



    public Event toEntity(EventRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setCapacity(request.capacity());
        event.setTicketPrice(request.ticketPrice());
        event.setCategory(request.category());
        return event;
    }

    public EventResponse toResponse(Event event) {

        OrganizerResponse organizerResponse = new OrganizerResponse(
                event.getOrganizer().getId(),
                event.getOrganizer().getUsername()
        );
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getCapacity(),
                event.getTicketPrice(),
                event.getCategory(),
                event.getStatus(),
                organizerResponse
        );
    }
}
