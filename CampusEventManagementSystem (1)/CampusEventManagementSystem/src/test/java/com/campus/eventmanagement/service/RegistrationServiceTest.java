package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.RegistrationDTO;
import com.campus.eventmanagement.entity.Event;
import com.campus.eventmanagement.entity.Registration;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.enums.RegistrationStatus;
import com.campus.eventmanagement.exception.EventFullException;
import com.campus.eventmanagement.mapper.RegistrationMapper;
import com.campus.eventmanagement.repository.EventRepository;
import com.campus.eventmanagement.repository.RegistrationRepository;
import com.campus.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the capacity-checking / @Transactional logic in
 * RegistrationService. Repositories are mocked so these run without a
 * database (course concept: unit testing the service layer in isolation).
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegistrationMapper registrationMapper;

    @InjectMocks
    private RegistrationService registrationService;

    private User student;
    private Event event;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).email("student@test.com").fullName("Ada Lovelace").build();
        event = Event.builder().id(10L).title("Career Fair").capacity(2).registeredCount(0).build();
    }

    @Test
    void registerForEvent_success_incrementsCountAndSavesRegistration() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.empty());
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        when(registrationMapper.toDTO(any(Registration.class))).thenReturn(RegistrationDTO.builder().eventId(10L).build());

        RegistrationDTO result = registrationService.registerForEvent(10L, "student@test.com");

        assertThat(result.getEventId()).isEqualTo(10L);
        assertThat(event.getRegisteredCount()).isEqualTo(1);

        ArgumentCaptor<Registration> captor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(RegistrationStatus.REGISTERED);
        verify(eventRepository).save(event);
    }

    @Test
    void registerForEvent_eventFull_throwsEventFullException() {
        event.setRegisteredCount(2); // capacity is 2 -> full
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(EventFullException.class,
                () -> registrationService.registerForEvent(10L, "student@test.com"));

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void registerForEvent_alreadyRegistered_throwsIllegalStateException() {
        Registration existing = Registration.builder()
                .user(student).event(event).status(RegistrationStatus.REGISTERED).build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerForEvent(10L, "student@test.com"));
    }

    @Test
    void cancelRegistration_decrementsCountAndMarksCancelled() {
        event.setRegisteredCount(1);
        Registration existing = Registration.builder()
                .user(student).event(event).status(RegistrationStatus.REGISTERED).build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(existing));

        registrationService.cancelRegistration(10L, "student@test.com");

        assertThat(existing.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(existing.getCancelledAt()).isNotNull();
        assertThat(event.getRegisteredCount()).isEqualTo(0);
        verify(eventRepository).save(event);
    }

    @Test
    void cancelRegistration_countNeverGoesNegative() {
        event.setRegisteredCount(0); // already at zero (defensive edge case)
        Registration existing = Registration.builder()
                .user(student).event(event).status(RegistrationStatus.REGISTERED).build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(existing));

        registrationService.cancelRegistration(10L, "student@test.com");

        assertThat(event.getRegisteredCount()).isEqualTo(0);
    }
}
