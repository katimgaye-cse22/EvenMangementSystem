package com.campus.eventmanagement.controller;

import com.campus.eventmanagement.dto.UserDTO;
import com.campus.eventmanagement.mapper.UserMapper;
import com.campus.eventmanagement.repository.UserRepository;
import com.campus.eventmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Self-service account registration + "who am I" lookup.
 * Actual login is handled by Spring Security's form-login filter (POST /login) -
 * see SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO request) {
        UserDTO created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(Authentication authentication) {
        UserDTO dto = userRepository.findByEmail(authentication.getName())
                .map(userMapper::toDTO)
                .orElseThrow();
        return ResponseEntity.ok(dto);
    }
}
