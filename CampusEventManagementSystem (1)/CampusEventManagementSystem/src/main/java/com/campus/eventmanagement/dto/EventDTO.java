package com.campus.eventmanagement.dto;

import com.campus.eventmanagement.validation.ValidEventTimes;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidEventTimes
public class EventDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Integer registeredCount;
    private Integer availableSeats;
    private Boolean full;

    private Long createdById;
    private String createdByName;
}
