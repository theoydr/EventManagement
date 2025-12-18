package com.github.theoydr.eventmanagement.repository;

import com.github.theoydr.eventmanagement.enums.EventStatus;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {


    List<Event> findByOrganizer(User organizer);


    List<Event> findByStatus(EventStatus status);


    List<Event> findByStartDateTimeAfter(LocalDateTime dateTime);

    boolean existsByOrganizerAndStartDateTimeAndLocation(User organizer, LocalDateTime startDateTime, String location);

}
