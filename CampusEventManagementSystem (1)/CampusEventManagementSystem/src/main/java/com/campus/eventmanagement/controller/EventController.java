package com.campus.eventmanagement.controller;

import com.campus.eventmanagement.dto.EventDTO;
import com.campus.eventmanagement.service.EventService;
import com.campus.eventmanagement.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Browsing/searching is open to everyone (matches proposal: students browse
 * without necessarily being logged in yet); create/update/delete is admin-only,
 * enforced with @PreAuthorize (course concept: RESTful API + role-based access).
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Fields safe to sort by — whitelisted rather than accepting a raw
     * property name from the client, which would otherwise let a caller
     * probe for (or 500 on) arbitrary/invalid entity fields.
     */
    private static final java.util.Set<String> SORTABLE_FIELDS =
            java.util.Set.of("eventDate", "title", "capacity", "registeredCount");

    @GetMapping
    public ResponseEntity<Page<EventDTO>> getEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_FIELD) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        int safeSize = Math.min(size, Constants.MAX_PAGE_SIZE);
        String safeSortField = SORTABLE_FIELDS.contains(sortBy) ? sortBy : Constants.DEFAULT_SORT_FIELD;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(direction, safeSortField));
        return ResponseEntity.ok(eventService.searchEvents(keyword, category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(eventService.getCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO dto, Authentication authentication) {
        EventDTO created = eventService.createEvent(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @Valid @RequestBody EventDTO dto) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
