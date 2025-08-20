// path: src/main/java/com/springapplication/studybuddyapp/api/dto/SignupRequest.java
package com.springapplication.studybuddyapp.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for /auth/signup.
 * Accepts username (stored as display name), email and raw password.
 */
public class SignupRequest {

    @NotBlank @Size(min = 2, max = 100)
    private String username;

    @NotBlank @Email @Size(max = 150)
    private String email;

    /**
     * Password rules are enforced in service-level validation:
     * - length 8-72
     * - at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
     */
    @NotBlank @Size(min = 8, max = 72)
    private String password;

    // getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

