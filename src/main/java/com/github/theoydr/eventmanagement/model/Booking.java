package com.github.theoydr.eventmanagement.model;


import com.github.theoydr.eventmanagement.constants.MessageKeys;
import com.github.theoydr.eventmanagement.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"}, name = "unique_user_event_booking")
})
@Checks({
        @Check(constraints = "number_of_tickets > 0", name = "booking_tickets_positive_check"),
        @Check(constraints = "booking_date_time <= CURRENT_TIMESTAMP", name = "booking_date_in_past_check")
})public class Booking {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many bookings can belong to one event
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Many bookings can belong to one user
    // (Assuming you’ll have a User entity later)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Min(value = 1, message = MessageKeys.BookingMessages.TICKETS_MIN)
    private Integer numberOfTickets;

    @Column(nullable = false)
    @PastOrPresent(message = MessageKeys.BookingMessages.BOOKING_NOT_FUTURE)
    private LocalDateTime bookingDateTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    public Booking() {}

    public Booking(Event event, User user, Integer numberOfTickets, BookingStatus status) {
        this.event = event;
        this.user = user;
        this.numberOfTickets = numberOfTickets;
        this.status = status;
        this.bookingDateTime = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(Integer numberOfTickets) { this.numberOfTickets = numberOfTickets; }

    public LocalDateTime getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(LocalDateTime bookingDateTime) { this.bookingDateTime = bookingDateTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    // equals & hashCode (based on id)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;

        return Objects.equals(user, booking.user) &&
                Objects.equals(event, booking.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, event);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", eventId=" + (event != null ? event.getId() : "null") + // SAFE
                ", userId=" + (user != null ? user.getId() : "null") + // SAFE
                ", numberOfTickets=" + numberOfTickets +
                ", bookingDateTime=" + bookingDateTime +
                ", status=" + status +
                '}';
    }
}