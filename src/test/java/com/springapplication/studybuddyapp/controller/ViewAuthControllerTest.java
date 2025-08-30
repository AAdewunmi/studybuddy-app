package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.service.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ViewAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceInterface userService;

    @InjectMocks
    private ViewAuthController viewAuthController;


    @BeforeEach
    public void setUp() {
        reset(userService);
    }

    @Test
    void shouldReturnLoginPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void shouldReturnSignupPageWithEmptyForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("signupForm"));
    }

    @Test
    void shouldRedirectToLoginWhenSignupIsSuccessful() throws Exception {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        String password = "Password123!";
        String passwordConfirm = "Password123!";

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordConfirm", passwordConfirm)
                        .with(csrf()))  // Add CSRF token here
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"))
                .andReturn();

        // Then
        verify(userService, times(1)).register(name, email, password);
    }


    @Test
    void shouldRedirectToSignupWithMismatchErrorWhenPasswordsDoNotMatch() throws Exception {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        String password = "Password123!";
        String passwordConfirm = "Password124!"; // Mismatch

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordConfirm", passwordConfirm)
                        .with(csrf()))  // Add CSRF token here
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup?mismatch"))  // Corrected the expected redirect URL
                .andReturn();
    }


    @Test
    void shouldHandleValidationErrorsForSignup() throws Exception {
        // When invalid email is provided
        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                        .param("name", "John Doe")
                        .param("email", "invalid-email") // Invalid email
                        .param("password", "Password123!")
                        .param("passwordConfirm", "Password123!")
                        .with(csrf()))  // Add CSRF token here
                .andExpect(status().isOk()) // Expect a 200 OK status
                .andExpect(view().name("signup"))
                .andExpect(model().attributeHasFieldErrors("signupForm", "email"));

        // When blank name is provided
        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                        .param("name", "") // Blank name
                        .param("email", "john@example.com")
                        .param("password", "Password123!")
                        .param("passwordConfirm", "Password123!")
                        .with(csrf()))  // Add CSRF token here
                .andExpect(status().isOk()) // Expect a 200 OK status
                .andExpect(view().name("signup"))
                .andExpect(model().attributeHasFieldErrors("signupForm", "name"));
    }


}
