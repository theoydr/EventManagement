package com.github.theoydr.eventmanagement.repository;

import com.github.theoydr.eventmanagement.model.Booking;
import com.github.theoydr.eventmanagement.model.Event;
import com.github.theoydr.eventmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    List<Booking> findByEvent(Event event);

    boolean existsByUserAndEvent(User user, Event event);
}