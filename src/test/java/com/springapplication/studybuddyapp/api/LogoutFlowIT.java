// path: src/test/java/com/springapplication/studybuddyapp/api/LogoutFlowIT.java
package com.springapplication.studybuddyapp.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full integration test: login creates an authenticated session, then logout
 * succeeds when a CSRF token is provided (prod-like behavior).
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=update")
class LogoutFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired UserRoleRepository userRoles;

    private final String email = "logout@test.com";
    private final String rawPassword = "Str0ngP@ss!";

    @BeforeEach
    void seedUser() {
        Role role = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        User u = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User nu = new User();
            nu.setName("Logout Test");
            nu.setEmail(email);
            nu.setPasswordHash(encoder.encode(rawPassword));
            return users.save(nu);
        });
        if (!userRoles.existsByUser_IdAndRole_Name(u.getId(), role.getName())) {
            userRoles.save(new UserRole(u, role));
        }
    }

    @Test
    void login_then_logout_withCsrf_succeeds() throws Exception {
        // 1) Login — creates session via /auth/login
        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email": "logout@test.com", "password": "Str0ngP@ss!" }
                """))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // 2) Logout — include CSRF token as required by prod SecurityConfig
        MvcResult logout = mvc.perform(
                        post("/logout")
                                .session(session)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andReturn();

        int status = logout.getResponse().getStatus();
        boolean okOrRedirect = (status == 200) || (status >= 300 && status < 400);
        assertThat(okOrRedirect)
                .withFailMessage("Expected 200 or 3xx on /logout, but was %s", status)
                .isTrue();
    }
}
