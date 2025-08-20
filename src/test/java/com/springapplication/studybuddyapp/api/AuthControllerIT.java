// path: src/test/java/com/springapplication/studybuddyapp/api/AuthControllerIT.java
package com.springapplication.studybuddyapp.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-stack signup test with real Postgres container.
 * Ensures endpoint returns 201 and persists user + role.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired MockMvc mvc;
    @Autowired RoleRepository roleRepo;

    @BeforeEach
    void seedRoles() {
        if (!roleRepo.existsByName("ROLE_USER")) roleRepo.save(new Role("ROLE_USER"));
    }

    @Test
    void signup_createsUserAndReturnsDto() throws Exception {
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username": "Alice",
                      "email": "alice@example.com",
                      "password": "StrongP@ss1"
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.roles", hasItem(anyOf(is("ROLE_USER"), is("USER"))))); // accepts either naming
    }

    @Test
    void signup_duplicateEmail_409() throws Exception {
        // first create
        mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "username":"Bob","email":"bob@example.com","password":"StrongP@ss1" }
                """)).andExpect(status().isCreated());

        // duplicate
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "username":"Bobby","email":"bob@example.com","password":"StrongP@ss1" }
                """))
                .andExpect(status().isConflict());
    }

    @Test
    void signup_weakPassword_400() throws Exception {
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "username":"Weak","email":"weak@example.com","password":"weak" }
                """))
                .andExpect(status().isBadRequest());
    }
}

