package de.jobst.resulter.adapter.driver.web.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidIdValidator.class })
public @interface ValidId {
    String message() default "The ID must be null or a positive number.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

