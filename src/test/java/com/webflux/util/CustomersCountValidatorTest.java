package com.webflux.util;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CustomersCountValidatorTest {

    @InjectMocks
    private CustomersCountValidator validator;

    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    public void testValidCustomersCount() {
        assertThat(validator.isValid(5, context)).isTrue();
        assertThat(validator.isValid(10, context)).isTrue();
        assertThat(validator.isValid(15, context)).isTrue();
    }

    @Test
    public void testInvalidCustomersCount() {
        assertThat(validator.isValid(7, context)).isFalse();
        assertThat(validator.isValid(8, context)).isFalse();
        assertThat(validator.isValid(0, context)).isFalse();
        assertThat(validator.isValid(20, context)).isFalse();
    }
}
