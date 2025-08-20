// path: src/main/java/com/springapplication/studybuddyapp/api/dto/UserResponse.java
package com.springapplication.studybuddyapp.api.dto;

import java.time.LocalDateTime;
import java.util.Set;

/** API response for a user. */
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Set<String> roles;
    private LocalDateTime createdAt;

    public UserResponse(Long id, String name, String email, Set<String> roles, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.email = email; this.roles = roles; this.createdAt = createdAt;
    }

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

