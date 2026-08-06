package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.EventDTO;
import com.campus.eventmanagement.entity.Event;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.exception.ResourceNotFoundException;
import com.campus.eventmanagement.mapper.EventMapper;
import com.campus.eventmanagement.repository.EventRepository;
import com.campus.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for browsing, searching, and (as an admin) managing events.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public Page<EventDTO> searchEvents(String keyword, String category, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();
        return eventRepository.searchEvents(normalizedKeyword, normalizedCategory, pageable)
                .map(eventMapper::toDTO);
    }

    public EventDTO getEventById(Long id) {
        Event event = findEventOrThrow(id);
        return eventMapper.toDTO(event);
    }

    public List<String> getCategories() {
        return eventRepository.findDistinctCategories();
    }

    public List<EventDTO> getUpcomingWithAvailableSeats() {
        return eventRepository.findUpcomingEventsWithAvailableSeats()
                .stream()
                .map(eventMapper::toDTO)
                .toList();
    }

    @Transactional
    public EventDTO createEvent(EventDTO dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("User", adminEmail));

        Event event = eventMapper.toEntity(dto);
        event.setCreatedBy(admin);

        Event saved = eventRepository.save(event);
        return eventMapper.toDTO(saved);
    }

    @Transactional
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event event = findEventOrThrow(id);

        // Guard against shrinking capacity below the number of students already registered.
        if (dto.getCapacity() != null && dto.getCapacity() < event.getRegisteredCount()) {
            throw new IllegalStateException(
                    "Capacity cannot be less than the number of students already registered ("
                            + event.getRegisteredCount() + ")");
        }

        eventMapper.updateEntityFromDTO(dto, event);
        Event saved = eventRepository.save(event);
        return eventMapper.toDTO(saved);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEventOrThrow(id);
        eventRepository.delete(event);
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Event", id));
    }
}
