package com.springapplication.studybuddyapp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web slice tests for login/signup pages and basic validation feedback.
 */
@WebMvcTest(controllers = {ViewAuthController.class})
@AutoConfigureMockMvc(addFilters = false) // view rendering only (no auth filter chain)
class ViewAuthControllerWebTest {

    @Autowired MockMvc mvc;

    @MockBean UserService userService;
    @MockBean UserRepository userRepository;

    @Test
    void loginPage_renders() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Welcome back")));
    }

    @Test
    void signup_validation_duplicateEmail() throws Exception {
        when(userRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);
        when(userRepository.existsByNameIgnoreCase("Alice")).thenReturn(false);

        mvc.perform(post("/signup")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Alice")
                        .param("email", "dup@example.com")
                        .param("password", "Password123!")
                        .param("passwordConfirm", "Password123!"))
                .andExpect(status().isOk()) // returns to form
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Email already in use")));
    }
}

