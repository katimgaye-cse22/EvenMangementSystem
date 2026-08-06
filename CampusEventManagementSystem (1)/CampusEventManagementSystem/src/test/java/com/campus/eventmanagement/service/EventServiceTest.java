package com.campus.eventmanagement.service;

import com.campus.eventmanagement.dto.EventDTO;
import com.campus.eventmanagement.entity.Event;
import com.campus.eventmanagement.exception.ResourceNotFoundException;
import com.campus.eventmanagement.mapper.EventMapper;
import com.campus.eventmanagement.repository.EventRepository;
import com.campus.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    @Test
    void getEventById_notFound_throwsResourceNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(99L));
    }

    @Test
    void updateEvent_capacityBelowRegisteredCount_throwsIllegalStateException() {
        Event event = Event.builder().id(5L).capacity(50).registeredCount(30).build();
        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));

        EventDTO dto = EventDTO.builder().capacity(10).build(); // fewer seats than already-registered students

        assertThrows(IllegalStateException.class, () -> eventService.updateEvent(5L, dto));
    }
}
