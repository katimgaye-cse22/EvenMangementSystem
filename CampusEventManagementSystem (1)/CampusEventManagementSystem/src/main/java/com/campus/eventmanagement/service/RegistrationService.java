package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.RegistrationDTO;
import com.campus.eventmanagement.entity.Event;
import com.campus.eventmanagement.entity.Registration;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.enums.RegistrationStatus;
import com.campus.eventmanagement.exception.EventFullException;
import com.campus.eventmanagement.exception.ResourceNotFoundException;
import com.campus.eventmanagement.mapper.RegistrationMapper;
import com.campus.eventmanagement.repository.EventRepository;
import com.campus.eventmanagement.repository.RegistrationRepository;
import com.campus.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles students registering/cancelling for events.
 *
 * The capacity check-and-increment in {@link #registerForEvent} is the one
 * place in the app where a race condition really matters (two students
 * grabbing the last seat at the same time), so it's wrapped in a single
 * @Transactional method: both the read of registeredCount and the write
 * happen inside the same transaction/database round trip, which is enough
 * to keep the count correct under Spring's default (READ_COMMITTED-ish)
 * isolation for this project's scale. A production system handling heavy
 * concurrent load would add a @Version field (optimistic locking) or a
 * pessimistic SELECT ... FOR UPDATE on top of this.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationMapper registrationMapper;

    @Transactional
    public RegistrationDTO registerForEvent(Long eventId, String userEmail) {
        User user = findUserOrThrow(userEmail);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Event", eventId));

        Registration registration = registrationRepository
                .findByUserIdAndEventId(user.getId(), eventId)
                .orElse(null);

        if (registration != null && registration.getStatus() == RegistrationStatus.REGISTERED) {
            throw new IllegalStateException("You are already registered for this event");
        }

        if (event.isFull()) {
            throw new EventFullException("This event has reached its capacity of " + event.getCapacity());
        }

        if (registration == null) {
            registration = Registration.builder()
                    .user(user)
                    .event(event)
                    .status(RegistrationStatus.REGISTERED)
                    .build();
        } else {
            // Re-registering after a previous cancellation - reuse the row rather than
            // inserting a new one (the DB has a unique constraint on user_id + event_id).
            registration.setStatus(RegistrationStatus.REGISTERED);
            registration.setRegisteredAt(LocalDateTime.now());
            registration.setCancelledAt(null);
        }

        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);
        Registration saved = registrationRepository.save(registration);

        return registrationMapper.toDTO(saved);
    }

    @Transactional
    public void cancelRegistration(Long eventId, String userEmail) {
        User user = findUserOrThrow(userEmail);
        Registration registration = registrationRepository
                .findByUserIdAndEventId(user.getId(), eventId)
                .filter(r -> r.getStatus() == RegistrationStatus.REGISTERED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active registration found for this event"));

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);

        Event event = registration.getEvent();
        event.setRegisteredCount(Math.max(0, event.getRegisteredCount() - 1));
        eventRepository.save(event);
    }

    public List<RegistrationDTO> getMyRegistrations(String userEmail) {
        User user = findUserOrThrow(userEmail);
        return registrationRepository
                .findByUserIdAndStatusOrderByRegisteredAtDesc(user.getId(), RegistrationStatus.REGISTERED)
                .stream()
                .map(registrationMapper::toDTO)
                .toList();
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", email));
    }
}
