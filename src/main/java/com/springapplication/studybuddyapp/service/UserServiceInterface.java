package com.springapplication.studybuddyapp.service;

import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.model.User;

public interface UserServiceInterface {
    boolean existsByEmail(String email);
    /**
     * Registers a new user with the given name, email, and raw password.
     * Should hash the password, assign ROLE_USER, and throw ConflictException if email exists.
     *
     * @param name        the full name
     * @param email       the email (case-insensitive)
     * @param rawPassword the plain password
     * @return
     * @throws ConflictException if email already registered
     */
    User register(String name, String email, String rawPassword);

    User findByEmail(String email);

    boolean emailExists(String email);
}

