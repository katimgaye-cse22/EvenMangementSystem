package com.campus.eventmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned as JSON by the custom Spring Security success/failure handlers
 * (see SecurityConfig) so the JS-driven login form can react without a
 * full page redirect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String message;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
}
