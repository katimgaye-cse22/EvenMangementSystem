package com.campus.eventmanagement.mapper;

import com.campus.eventmanagement.dto.EventDTO;
import com.campus.eventmanagement.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .capacity(event.getCapacity())
                .registeredCount(event.getRegisteredCount())
                .availableSeats(event.getAvailableSeats())
                .full(event.isFull())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .createdByName(event.getCreatedBy() != null ? event.getCreatedBy().getFullName() : null)
                .build();
    }

    /**
     * Builds a new Event entity from a create/update DTO.
     * id, registeredCount, createdBy, and timestamps are deliberately NOT set here -
     * those are managed by EventService (createdBy comes from the logged-in admin,
     * registeredCount is owned by RegistrationService).
     */
    public Event toEntity(EventDTO dto) {
        if (dto == null) {
            return null;
        }
        return Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .venue(dto.getVenue())
                .eventDate(dto.getEventDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .capacity(dto.getCapacity())
                .build();
    }

    /**
     * Applies editable fields from a DTO onto an existing managed entity (used by update).
     */
    public void updateEntityFromDTO(EventDTO dto, Event event) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setCategory(dto.getCategory());
        event.setVenue(dto.getVenue());
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setCapacity(dto.getCapacity());
    }
}
