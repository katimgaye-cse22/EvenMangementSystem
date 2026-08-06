package com.campus.eventmanagement.mapper;

import com.campus.eventmanagement.dto.RegistrationDTO;
import com.campus.eventmanagement.entity.Registration;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    public RegistrationDTO toDTO(Registration registration) {
        if (registration == null) {
            return null;
        }
        return RegistrationDTO.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .studentName(registration.getUser().getFullName())
                .studentIdNumber(registration.getUser().getStudentId())
                .studentEmail(registration.getUser().getEmail())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .cancelledAt(registration.getCancelledAt())
                .build();
    }
}
