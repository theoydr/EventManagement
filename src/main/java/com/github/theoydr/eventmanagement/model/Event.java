package com.github.theoydr.eventmanagement.model;

import com.github.theoydr.eventmanagement.constants.MessageKeys;
import com.github.theoydr.eventmanagement.enums.EventCategory;
import com.github.theoydr.eventmanagement.enums.EventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "events", uniqueConstraints = {
        // Adds a database-level constraint to prevent an organizer from creating duplicate events
        // based on the start time and location.
        @UniqueConstraint(columnNames = {"organizer_id", "start_date_time", "location"}, name = "unique_organizer_start_location")
})
@Checks({
        @Check(constraints = "end_date_time > start_date_time", name = "event_dates_check"),
        @Check(constraints = "capacity > 0", name = "event_capacity_check"),
        @Check(constraints = "ticket_price >= 0", name = "event_ticket_price_check"),
        @Check(constraints = "TRIM(title) <> ''", name = "event_title_not_blank_check"),
        @Check(constraints = "TRIM(location) <> ''", name = "event_location_not_blank_check")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private final UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    @Size(min = 3, max = 100, message = MessageKeys.EventMessages.TITLE_SIZE)
    private String title;  // Name of the event

    @Column(length = 2000)
    private String description;  // Detailed description

    @Column(nullable = false)
    private String location;  // Address or venue

    @Column(nullable = false)
    private LocalDateTime startDateTime;  // When it starts

    @Column(nullable = false)
    private LocalDateTime endDateTime;  // When it ends

    @Column(nullable = false)
    @Min(value = 1, message = MessageKeys.EventMessages.CAPACITY_MIN)
    private Integer capacity;  // Max number of attendees

    @Column(nullable = false)
    @Min(value = 0, message = MessageKeys.EventMessages.PRICE_MIN)
    private Double ticketPrice; // Price per ticket (could be 0.0 for free events)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Enumerated(EnumType.STRING) // Stores the enum as a readable string in DB
    @Column(nullable = false)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    public Event() {}

    public Event(String title, String description, String location, LocalDateTime startDateTime,
                 LocalDateTime endDateTime, Integer capacity, Double ticketPrice,
                 User organizer, EventCategory category, EventStatus status) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.capacity = capacity;
        this.ticketPrice = ticketPrice;
        this.organizer = organizer;
        this.category = category;
        this.status = status;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUuid() { return uuid; }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return uuid.equals(event.uuid);

    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", capacity=" + capacity +
                ", ticketPrice=" + ticketPrice +
                ", organizerId=" + (organizer != null ? organizer.getId() : "null") + // SAFE
                ", category=" + category +
                ", status=" + status +
                '}';
    }
}
