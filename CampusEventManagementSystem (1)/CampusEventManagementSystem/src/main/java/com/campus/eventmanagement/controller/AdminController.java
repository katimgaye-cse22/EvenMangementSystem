package com.campus.eventmanagement.controller;

import com.campus.eventmanagement.dto.RegistrationDTO;
import com.campus.eventmanagement.repository.projection.EventPopularityProjection;
import com.campus.eventmanagement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only reporting endpoints: participant lists and CSV export
 * ("Generate participant lists" feature from the proposal).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/events/{eventId}/participants")
    public ResponseEntity<List<RegistrationDTO>> getParticipants(@PathVariable Long eventId) {
        return ResponseEntity.ok(adminService.getParticipants(eventId));
    }

    @GetMapping("/events/{eventId}/participants/export")
    public ResponseEntity<String> exportParticipants(@PathVariable Long eventId) {
        String csv = adminService.generateParticipantsCsv(eventId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"event-" + eventId + "-participants.csv\"")
                .body(csv);
    }

    @GetMapping("/reports/popular-events")
    public ResponseEntity<List<EventPopularityProjection>> getPopularEvents(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(adminService.getMostPopularEvents(limit));
    }
}
