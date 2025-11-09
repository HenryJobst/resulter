package de.jobst.resulter.adapter.driver.web.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidIdValidator implements ConstraintValidator<ValidId, Long> {
    @Override public boolean isValid(Long value, ConstraintValidatorContext context) {
        return null == value || value > 0;
    }
}

