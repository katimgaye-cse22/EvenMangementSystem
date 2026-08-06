package com.campus.eventmanagement.repository;

import com.campus.eventmanagement.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE "
            + "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "     OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:category IS NULL OR e.category = :category) "
            + "ORDER BY e.eventDate ASC")
    Page<Event> searchEvents(@Param("keyword") String keyword,
                             @Param("category") String category,
                             Pageable pageable);

    @Query("SELECT DISTINCT e.category FROM Event e ORDER BY e.category")
    List<String> findDistinctCategories();

    @Query(value = "SELECT * FROM events e "
            + "WHERE e.event_date >= CURDATE() AND e.registered_count < e.capacity "
            + "ORDER BY e.event_date ASC, e.start_time ASC",
            nativeQuery = true)
    List<Event> findUpcomingEventsWithAvailableSeats();
}