package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.RegistrationDTO;
import com.campus.eventmanagement.entity.Event;
import com.campus.eventmanagement.enums.RegistrationStatus;
import com.campus.eventmanagement.exception.ResourceNotFoundException;
import com.campus.eventmanagement.mapper.RegistrationMapper;
import com.campus.eventmanagement.repository.EventRepository;
import com.campus.eventmanagement.repository.RegistrationRepository;
import com.campus.eventmanagement.repository.projection.EventPopularityProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin-only reporting: participant lists per event, and a CSV export
 * ("Generate participant lists" feature from the proposal).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;

    public List<RegistrationDTO> getParticipants(Long eventId) {
        ensureEventExists(eventId);
        return registrationRepository
                .findByEventIdAndStatusOrderByRegisteredAtAsc(eventId, RegistrationStatus.REGISTERED)
                .stream()
                .map(registrationMapper::toDTO)
                .toList();
    }

    /**
     * Top N events by registration count (course concept: JPQL aggregate
     * query with GROUP BY).
     */
    public List<EventPopularityProjection> getMostPopularEvents(int limit) {
        return registrationRepository.findMostPopularEvents(
                RegistrationStatus.REGISTERED, PageRequest.of(0, limit));
    }

    /**
     * Builds a CSV file (as a String) listing every currently-registered
     * participant for an event, for the admin to download.
     */
    public String generateParticipantsCsv(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Event", eventId));

        List<RegistrationDTO> participants = getParticipants(eventId);

        StringBuilder csv = new StringBuilder();
        csv.append("Event: ").append(event.getTitle()).append("\n");
        csv.append("Student Name,Student ID,Email,Registered At\n");

        for (RegistrationDTO p : participants) {
            csv.append(escapeCsv(p.getStudentName())).append(',')
                    .append(escapeCsv(p.getStudentIdNumber())).append(',')
                    .append(escapeCsv(p.getStudentEmail())).append(',')
                    .append(p.getRegisteredAt()).append('\n');
        }

        return csv.toString();
    }

    private void ensureEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw ResourceNotFoundException.forEntity("Event", eventId);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
