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
            User nu = new User("Admin", email, encoder.encode("AdminP@ss1"));
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

        MvcResult login = mvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "admin@test.com")
                        .param("password", "AdminP@ss1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dashboard")));

        mvc.perform(get("/groups").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your Groups")));

        mvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void member_signup_then_login_view_dashboard_groups_logout() throws Exception {
        mvc.perform(get("/signup"))
                .andExpect(status().isOk());

        mvc.perform(post("/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Member1")
                        .param("email", "member1@test.com")
                        .param("password", "MemberP@ss1")
                        .param("passwordConfirm", "MemberP@ss1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login*"));

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

        mvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dashboard")));

        mvc.perform(get("/groups").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your Groups")));

        mvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection());
    }
}
