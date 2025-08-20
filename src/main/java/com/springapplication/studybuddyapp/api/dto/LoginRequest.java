// path: src/main/java/com/springapplication/studybuddyapp/api/dto/LoginRequest.java
package com.springapplication.studybuddyapp.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for POST /auth/login.
 * Uses email as the username field.
 */
public class LoginRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    // getters/setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

