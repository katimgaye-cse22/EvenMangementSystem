package com.campus.eventmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A campus event that students can browse and register for.
 * Capacity is enforced in the service layer (EventService) inside a
 * @Transactional block to avoid race conditions on concurrent registrations.
 */
@Entity
@Table(
        name = "events",
        indexes = {
                // Speeds up "search by title" and "filter by category" (course concept: Database Indexing)
                @Index(name = "idx_event_title", columnList = "title"),
                @Index(name = "idx_event_category", columnList = "category"),
                @Index(name = "idx_event_date", columnList = "event_date")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "registrations")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optimistic-locking version (course concept: ACID / concurrency control).
     * Hibernate increments this on every UPDATE and checks it in the WHERE
     * clause; if two admins/registrations race to update the same row,
     * the second write fails fast with an OptimisticLockingFailureException
     * instead of silently overwriting the first change.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Venue is required")
    @Column(nullable = false, length = 150)
    private String venue;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private Integer capacity;

    /**
     * Denormalized counter kept in sync by RegistrationService inside a transaction.
     * Avoids a COUNT(*) query every time we need to check remaining seats.
     */
    @Column(name = "registered_count", nullable = false)
    @Builder.Default
    private Integer registeredCount = 0;

    /**
     * The admin who created this event.
     * Many events -> one admin user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * One event has many registrations (students who signed up).
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @Transient
    public int getAvailableSeats() {
        return capacity - registeredCount;
    }

    @Transient
    public boolean isFull() {
        return registeredCount >= capacity;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.registeredCount == null) {
            this.registeredCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
