// path: src/test/java/com/springapplication/studybuddyapp/api/LoginLogoutFlowIT.java
package com.springapplication.studybuddyapp.api;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end: login establishes a session; logout returns JSON message.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=update")
class LoginLogoutFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired UserRoleRepository userRoles;

    private final String email = "flow@test.com";
    private final String rawPassword = "Str0ngP@ss!";

    @BeforeEach
    void seedUser() {
        Role role = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        User u = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User nu = new User();
            nu.setName("Flow Test");
            nu.setEmail(email);
            nu.setPasswordHash(encoder.encode(rawPassword));
            return users.save(nu);
        });
        if (!userRoles.existsByUser_IdAndRole_Name(u.getId(), role.getName())) {
            userRoles.save(new UserRole(u, role));
        }
    }

    @Test
    void login_then_logout_returnsOkJson() throws Exception {
        // Login
        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email": "flow@test.com", "password": "Str0ngP@ss!" }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // Logout (CSRF ignored for /logout in production config above)
        mvc.perform(post("/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"message\":\"Logout successful\"}"));
    }
}

