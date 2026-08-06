package com.campus.eventmanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Class-level constraint on EventDTO: ensures endTime is strictly after
 * startTime. A field-level @NotNull can't compare two sibling fields,
 * which is why this needs to be its own ConstraintValidator (course
 * concept: custom Bean Validation).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventTimesValidator.class)
@Documented
public @interface ValidEventTimes {
    String message() default "End time must be after start time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
