package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.UserDTO;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.enums.Role;
import com.campus.eventmanagement.mapper.UserMapper;
import com.campus.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles account creation. Actual login/authentication is delegated to
 * Spring Security's form-login filter (see SecurityConfig) - this service
 * only needs to cover self-service registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO register(UserDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("An account with this email already exists");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalStateException("An account with this student ID already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Self-service signup always creates a STUDENT account; admin accounts
        // are provisioned separately (data.sql / by another admin), never via this endpoint.
        user.setRole(Role.STUDENT);

        User saved = userRepository.save(user);
        return userMapper.toDTO(saved);
    }
}
