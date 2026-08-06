package com.campus.eventmanagement.mapper;

import com.campus.eventmanagement.dto.UserDTO;
import com.campus.eventmanagement.entity.User;
import org.springframework.stereotype.Component;

/**
 * Manual entity <-> DTO mapping (course concept: DTO pattern).
 * Kept simple/explicit rather than pulling in MapStruct, since the
 * mapping surface here is small and this stays easy to read.
 */
@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        // Note: password is intentionally never copied into the outgoing DTO.
    }

    /**
     * Builds a new User entity from a registration DTO.
     * Password encoding and role assignment are handled by AuthService,
     * not here, since those are security decisions rather than plain mapping.
     */
    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .fullName(dto.getFullName())
                .studentId(dto.getStudentId())
                .email(dto.getEmail())
                .build();
    }
}
