// path: src/main/java/com/springapplication/studybuddyapp/service/AuthService.java
package com.springapplication.studybuddyapp.service;

import com.springapplication.studybuddyapp.api.dto.UserResponse;
import com.springapplication.studybuddyapp.exception.BadRequestException;
import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.exception.NotFoundException;
import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication-related actions (e.g., signup).
 */
@Service
@Transactional
public class AuthService {

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            // at least 1 lower, 1 upper, 1 digit, 1 special, total length validated by annotation/service
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]).{8,72}$"
    );

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, RoleRepository roles, UserRoleRepository userRoles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.encoder = encoder;
    }

    /**
     * Sign up a new user.
     * - Ensures unique email
     * - Validates password strength
     * - Hashes password with BCrypt
     * - Assigns default role ROLE_USER (falls back to USER if that's what's present)
     */
    public UserResponse signup(String username, String email, String rawPassword) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already in use: " + email);
        }
        if (rawPassword == null || !STRONG_PASSWORD.matcher(rawPassword).matches()) {
            throw new BadRequestException("Password must be 8-72 chars and include upper, lower, digit, special.");
        }

        User u = new User();
        u.setName(username);                               // mapping username -> display name column
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        u = users.save(u);

        // Prefer ROLE_USER, but support older USER naming if your DB has that
        Role defaultRole = roles.findByName("ROLE_USER")
                .orElseGet(() -> roles.findByName("USER")
                        .orElseThrow(() -> new NotFoundException("Default role ROLE_USER/USER not found")));
        userRoles.save(new UserRole(u, defaultRole));

        // reload with roles and map to response
        User reloaded = users.findByIdWithRoles(u.getId()).orElseThrow();
        return toResponse(reloaded);
    }

    private UserResponse toResponse(User u) {
        Set<String> roleNames = u.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), roleNames, u.getCreatedAt());
    }
}

