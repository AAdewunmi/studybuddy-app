package com.springapplication.studybuddyapp.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
 * End-to-end UI flows:
 * 1) Admin logs in, visits dashboard and groups, logs out.
 * 2) Member signs up (web), logs in, visits dashboard and groups, logs out.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=update")
class UiFlowsIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired UserRoleRepository userRoles;

    @BeforeEach
    void seedRoles() {
        if (!roles.existsByName("ROLE_USER")) roles.save(new Role("ROLE_USER"));
        if (!roles.existsByName("ROLE_ADMIN")) roles.save(new Role("ROLE_ADMIN"));
    }

    private void ensureAdminUser() {
        var email = "admin@test.com";
        User u = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User nu = new User();
            nu.setName("Admin");
            nu.setEmail(email);
            nu.setPasswordHash(encoder.encode("AdminP@ss1"));
            return users.save(nu);
        });
        var adminRole = roles.findByName("ROLE_ADMIN").orElseThrow();
        var userRole = roles.findByName("ROLE_USER").orElseThrow();
        if (!userRoles.existsByUser_IdAndRole_Name(u.getId(), adminRole.getName())) {
            userRoles.save(new UserRole(u, adminRole));
        }
        if (!userRoles.existsByUser_IdAndRole_Name(u.getId(), userRole.getName())) {
            userRoles.save(new UserRole(u, userRole));
        }
    }

    @Test
    void admin_can_login_view_dashboard_groups_logout() throws Exception {
        ensureAdminUser();

        // Login form (email/password)
        MvcResult login = mvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "admin@test.com")
                        .param("password", "AdminP@ss1"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // Dashboard accessible
        mvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("Dashboard")));

        // Groups accessible
        mvc.perform(get("/groups").session(session))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("Your Groups")));

        // Logout via POST (include CSRF)
        MvcResult logout = mvc.perform(post("/logout")
                        .with(csrf())
                        .session(session))
                .andReturn();

        int code = logout.getResponse().getStatus();
        // default is 302 -> /login?logout
        assertThat(code >= 200 && code < 400).isTrue();
    }


    @Test
    void member_signup_then_login_view_dashboard_groups_logout() throws Exception {
        // 1) GET signup
        mvc.perform(get("/signup"))
                .andExpect(status().isOk());

        // 2) POST signup (form)
        mvc.perform(post("/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Member1")
                        .param("email", "member1@test.com")
                        .param("password", "MemberP@ss1")
                        .param("passwordConfirm", "MemberP@ss1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login*"));

        // 3) POST login (form) and CAPTURE SESSION (do not assume Set-Cookie exists)
        MvcResult login = mvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "member1@test.com")
                        .param("password", "MemberP@ss1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).as("session from login").isNotNull();

        // 4) Authenticated GETs using the session
        mvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dashboard")));

        mvc.perform(get("/groups").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your Groups")));

        // 5) Logout (POST with CSRF) using the session
        MvcResult logout = mvc.perform(post("/logout").with(csrf()).session(session))
                .andReturn();
        int status = logout.getResponse().getStatus();
        assertThat(status >= 200 && status < 400).isTrue();
    }

}

