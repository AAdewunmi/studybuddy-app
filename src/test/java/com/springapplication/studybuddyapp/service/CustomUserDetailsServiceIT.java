package com.springapplication.studybuddyapp.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies that CustomUserDetailsService loads a user with roles without LAZY issues.
 */
@Testcontainers
@SpringBootTest
class CustomUserDetailsServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired CustomUserDetailsService uds;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordEncoder encoder;

    private final String email = "auth@test.com";

    @BeforeEach
    void seed() {
        Role user = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        User u = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User nu = new User();
            nu.setName("Auth Test");
            nu.setEmail(email);
            nu.setPasswordHash(encoder.encode("Str0ngP@ss!"));
            return users.save(nu);
        });
        if (!userRoles.existsByUser_IdAndRole_Name(u.getId(), user.getName())) {
            userRoles.save(new UserRole(u, user));
        }
    }

    @Test
    void loads_user_with_roles() {
        UserDetails details = uds.loadUserByUsername(email);
        assertThat(details.getUsername()).isEqualTo(email);
        assertThat(details.getAuthorities().stream().map(a -> a.getAuthority()))
                .contains("ROLE_USER");
    }
}

