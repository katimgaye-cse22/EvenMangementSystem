package com.campus.eventmanagement.dto;

import com.campus.eventmanagement.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {

    private Long id;

    private Long userId;
    private String studentName;
    private String studentIdNumber;
    private String studentEmail;

    private Long eventId;
    private String eventTitle;

    private RegistrationStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
}
