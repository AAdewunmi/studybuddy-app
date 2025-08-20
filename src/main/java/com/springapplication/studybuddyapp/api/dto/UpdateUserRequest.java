package com.springapplication.studybuddyapp.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body to update profile (name/email). */
public class UpdateUserRequest {
    @NotBlank @Size(min = 2, max = 100)
    private String name;

    @NotBlank @Email @Size(max = 150)
    private String email;

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

