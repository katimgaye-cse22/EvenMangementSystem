package com.campus.eventmanagement.entity;

import com.campus.eventmanagement.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents both students and administrators.
 * Distinguished by the {@link Role} field, which Spring Security maps to
 * an authority (ROLE_STUDENT / ROLE_ADMIN).
 */
@Entity
@Table(
        name = "users",
        indexes = {
                // Index for fast login lookups (Database Indexing concept)
                @Index(name = "idx_user_email", columnList = "email", unique = true),
                @Index(name = "idx_user_student_id", columnList = "student_id", unique = true)
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "registrations")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false, length = 100)
    private String fullName;

    /**
     * Unique student/admin identifier used at this university
     * (kept separate from the primary key so it can be reissued/formatted independently).
     */
    @NotBlank(message = "Student ID is required")
    @Column(name = "student_id", nullable = false, unique = true, length = 30)
    private String studentId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.STUDENT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * One user (student) can have many registrations.
     * mappedBy = "user" -> Registration.user owns the foreign key (many-to-one side).
     * cascade + orphanRemoval so deleting a user cleans up their registration history.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
