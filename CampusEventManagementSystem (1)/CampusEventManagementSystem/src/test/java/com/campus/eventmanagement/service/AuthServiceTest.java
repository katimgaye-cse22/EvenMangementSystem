package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.UserDTO;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.enums.Role;
import com.campus.eventmanagement.mapper.UserMapper;
import com.campus.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateEmail_throwsIllegalStateException() {
        UserDTO request = UserDTO.builder()
                .fullName("Grace Hopper").studentId("S-100")
                .email("grace@test.com").password("SuperSecret1").build();

        when(userRepository.existsByEmail("grace@test.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_success_encodesPasswordAndForcesStudentRole() {
        UserDTO request = UserDTO.builder()
                .fullName("Grace Hopper").studentId("S-100")
                .email("grace@test.com").password("SuperSecret1").build();

        User mappedEntity = User.builder()
                .fullName("Grace Hopper").studentId("S-100").email("grace@test.com").build();

        when(userRepository.existsByEmail("grace@test.com")).thenReturn(false);
        when(userRepository.existsByStudentId("S-100")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mappedEntity);
        when(passwordEncoder.encode("SuperSecret1")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDTO(any(User.class))).thenReturn(UserDTO.builder().email("grace@test.com").role(Role.STUDENT).build());

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.STUDENT);
    }
}
