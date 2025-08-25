package com.springapplication.studybuddyapp.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

/**
 * Request body for signup (used by API and form flow).
 *
 * <p>Validations:</p>
 * <ul>
 *   <li><b>name</b>: required, 2-100 chars</li>
 *   <li><b>email</b>: required, valid email, max 150 chars</li>
 *   <li><b>password</b>: required, 8-72 chars</li>
 *   <li><b>passwordConfirm</b>: must match password (class-level check via {@link #isPasswordConfirmed()})</li>
 * </ul>
 *
 * <p>Note: We accept both "name" and "username" JSON keys to avoid breaking clients.</p>
 */
public class SignupRequest {

    /**
     * Display name for the user.
     * Accepts JSON keys "name" or legacy "username".
     */
    @NotBlank
    @Size(min = 2, max = 100)
    @JsonAlias({"username"})
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    /**
     * Password rules are enforced at service too, but we require minimum length here.
     * BCrypt safe max is 72 characters.
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    /**
     * Confirmation field for form flows. Not persisted; just validated.
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String passwordConfirm;

    /**
     * Class-level style check exposed as a bean property.
     * When false, Bean Validation will report "Passwords do not match".
     */
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {
        if (password == null || passwordConfirm == null) return false;
        return password.equals(passwordConfirm);
    }

    // Getters / Setters

    /** @return display name */
    public String getName() {
        return name;
    }

    /** @param name display name (2-100 chars) */
    public void setName(String name) {
        this.name = name;
    }

    /** @return email address */
    public String getEmail() {
        return email;
    }

    /** @param email valid email (max 150 chars) */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return raw password (8-72 chars) */
    public String getPassword() {
        return password;
    }

    /** @param password raw password (8-72 chars) */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return password confirmation */
    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    /** @param passwordConfirm must match {@link #password} */
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}

