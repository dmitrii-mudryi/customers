package com.webflux.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class CustomersCountValidator implements ConstraintValidator<ValidCustomersCount, Integer> {

    private final List<Integer> validCounts = Arrays.asList(5, 10, 15);

    @Override
    public void initialize(ValidCustomersCount constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && validCounts.contains(value);
    }
}
