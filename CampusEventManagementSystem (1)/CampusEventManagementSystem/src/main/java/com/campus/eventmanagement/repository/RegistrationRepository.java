package com.campus.eventmanagement.repository;

import com.campus.eventmanagement.entity.Registration;
import com.campus.eventmanagement.enums.RegistrationStatus;
import com.campus.eventmanagement.repository.projection.EventPopularityProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, RegistrationStatus status);

    List<Registration> findByUserIdAndStatusOrderByRegisteredAtDesc(Long userId, RegistrationStatus status);

    List<Registration> findByEventIdAndStatusOrderByRegisteredAtAsc(Long eventId, RegistrationStatus status);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    /**
     * Aggregate JPQL query (GROUP BY + COUNT) for an admin report: which
     * events are drawing the most registrations. Returns a projection
     * instead of entities since we only need three columns.
     */
    @Query("SELECT r.event.id AS eventId, r.event.title AS eventTitle, COUNT(r) AS registrationCount "
            + "FROM Registration r "
            + "WHERE r.status = :status "
            + "GROUP BY r.event.id, r.event.title "
            + "ORDER BY COUNT(r) DESC")
    List<EventPopularityProjection> findMostPopularEvents(@Param("status") RegistrationStatus status, Pageable pageable);
}
