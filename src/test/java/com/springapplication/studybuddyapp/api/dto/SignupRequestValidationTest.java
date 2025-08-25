package com.springapplication.studybuddyapp.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic Bean Validation tests for SignupRequest.
 */
class SignupRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void valid_request_has_no_violations() {
        SignupRequest dto = new SignupRequest();
        dto.setName("Ada Lovelace");
        dto.setEmail("ada@example.com");
        dto.setPassword("SecretP@ss1");
        dto.setPasswordConfirm("SecretP@ss1");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void password_mismatch_triggers_global_violation() {
        SignupRequest dto = new SignupRequest();
        dto.setName("Grace Hopper");
        dto.setEmail("grace@example.com");
        dto.setPassword("SecretP@ss1");
        dto.setPasswordConfirm("DifferentP@ss1");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v -> "Passwords do not match".equals(v.getMessage()));
    }

    @Test
    void invalid_fields_are_reported() {
        SignupRequest dto = new SignupRequest();
        dto.setName("");                 // NotBlank
        dto.setEmail("not-an-email");    // Email
        dto.setPassword("short");        // Size(min=8)
        dto.setPasswordConfirm("short"); // Size(min=8) + matches, so no global mismatch error

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(dto);

        // Collect property paths to strings before asserting
        Set<String> paths = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(paths).contains("name", "email", "password", "passwordConfirm");
    }
}

