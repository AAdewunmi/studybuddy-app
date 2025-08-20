package com.springapplication.studybuddyapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void bCryptMatches() {
        //var encoder = new BCryptPasswordEncoder(12);
        //String raw = "MyS3cret!";
        //String hash = encoder.encode(raw);

        //assertNotEquals(raw, hash);
        //assertTrue(encoder.matches(raw, hash));
        //assertFalse(encoder.matches("wrong", hash));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String rawPassword = "MyS3cret!";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Encoded: " + encodedPassword);

        assertNotEquals(rawPassword, encodedPassword); // should not be equal (hashed)
        assertTrue(encoder.matches(rawPassword, encodedPassword)); // should pass
        assertFalse(encoder.matches("wrongPassword", encodedPassword)); // should fail
    }
}

