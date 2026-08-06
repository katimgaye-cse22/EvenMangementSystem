package com.campus.eventmanagement.controller;

import com.campus.eventmanagement.dto.RegistrationDTO;
import com.campus.eventmanagement.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student-facing registration actions. Only STUDENT accounts register for
 * events; admins manage events via EventController/AdminController instead.
 */
@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/api/events/{eventId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RegistrationDTO> register(@PathVariable Long eventId, Authentication authentication) {
        RegistrationDTO dto = registrationService.registerForEvent(eventId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/api/events/{eventId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> cancel(@PathVariable Long eventId, Authentication authentication) {
        registrationService.cancelRegistration(eventId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/registrations/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<RegistrationDTO>> myRegistrations(Authentication authentication) {
        return ResponseEntity.ok(registrationService.getMyRegistrations(authentication.getName()));
    }
}
