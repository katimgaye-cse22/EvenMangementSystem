package com.campus.eventmanagement.validation;

import com.campus.eventmanagement.dto.EventDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventTimesValidator implements ConstraintValidator<ValidEventTimes, EventDTO> {

    @Override
    public boolean isValid(EventDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getStartTime() == null || dto.getEndTime() == null) {
            // Let @NotNull on the individual fields report those errors separately.
            return true;
        }
        boolean valid = dto.getEndTime().isAfter(dto.getStartTime());
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("End time must be after start time")
                    .addPropertyNode("endTime")
                    .addConstraintViolation();
        }
        return valid;
    }
}
