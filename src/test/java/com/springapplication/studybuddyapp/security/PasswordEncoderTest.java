package com.springapplication.studybuddyapp.security;

import com.springapplication.studybuddyapp.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void bCryptMatches() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String rawPassword = "MyS3cret!";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Encoded: " + encodedPassword);

        assertNotEquals(rawPassword, encodedPassword); // should not be equal (hashed)
        assertTrue(encoder.matches(rawPassword, encodedPassword)); // should pass
        assertFalse(encoder.matches("wrongPassword", encodedPassword)); // should fail
    }

    @Test
    void passwordEncoder_isBCrypt() {
        SecurityConfig cfg = new SecurityConfig();
        PasswordEncoder encoder = cfg.passwordEncoder();

        assertNotNull(encoder, "PasswordEncoder bean should not be null");
        assertTrue(encoder instanceof BCryptPasswordEncoder,
                "PasswordEncoder must be BCryptPasswordEncoder");
    }
}

