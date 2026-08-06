package com.campus.eventmanagement.entity;

import com.campus.eventmanagement.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Join entity between User (student) and Event, with extra attributes
 * (status, timestamps) — which is why this is modeled as its own entity
 * rather than a plain @ManyToMany.
 */
@Entity
@Table(
        name = "registrations",
        uniqueConstraints = {
                // A student can only have one ACTIVE registration row per event;
                // re-registering after cancelling is handled in the service layer.
                @UniqueConstraint(name = "uk_user_event", columnNames = {"user_id", "event_id"})
        },
        indexes = {
                @Index(name = "idx_registration_user", columnList = "user_id"),
                @Index(name = "idx_registration_event", columnList = "event_id"),
                @Index(name = "idx_registration_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}
