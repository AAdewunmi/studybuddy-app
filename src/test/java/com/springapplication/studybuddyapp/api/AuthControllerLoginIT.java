// path: src/test/java/com/springapplication/studybuddyapp/api/AuthControllerLoginIT.java
package com.springapplication.studybuddyapp.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full integration: spins a Postgres container, seeds a user,
 * verifies /auth/login success and failure paths.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=update") // create schema in container
class AuthControllerLoginIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired UserRoleRepository userRoles;

    private String email = "login@test.com";
    private String rawPassword = "Str0ngP@ss!";

    @BeforeEach
    void seedUser() {
        // ensure role exists
        Role role = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));

        // create user if not present
        User u = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User nu = new User();
            nu.setName("Login Test");
            nu.setEmail(email);
            nu.setPasswordHash(encoder.encode(rawPassword));
            return users.save(nu);
        });

        // link role if missing
        if (!userRoles.existsByUser_IdAndRole_Name(u.getId(), role.getName())) {
            userRoles.save(new UserRole(u, role));
        }
    }

    @Test
    void login_success_200() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email": "login@test.com", "password": "Str0ngP@ss!" }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_wrongPassword_401() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email": "login@test.com", "password": "wrong" }
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}

