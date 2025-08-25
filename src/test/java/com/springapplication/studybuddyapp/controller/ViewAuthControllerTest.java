package com.springapplication.studybuddyapp.controller;
import com.springapplication.studybuddyapp.service.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ViewAuthController focusing on business logic without Spring context.
 * This approach avoids template resolution issues and provides fast, isolated testing.
 */
@ExtendWith(MockitoExtension.class)
class ViewAuthControllerTest {

    private static final String VALID_EMAIL = "valid@example.com";
    private static final String DUPLICATE_EMAIL = "duplicate@example.com";
    private static final String VALID_PASSWORD = "ValidPass123!";
    private static final String DIFFERENT_PASSWORD = "DifferentPass123!";
    private static final String SIGNUP_VIEW = "auth/signup";
    private static final String LOGIN_REDIRECT = "redirect:/login?registered";

    @Mock
    private UserServiceInterface userService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    private ViewAuthController controller;

    @BeforeEach
    void setUp() {
        controller = new ViewAuthController(userService);
    }

    @Test
    void handleSignup_WithValidForm_ShouldRedirectToLogin() {
        SignupForm form = createValidForm();
        when(userService.existsByEmail(VALID_EMAIL)).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

        assertEquals(LOGIN_REDIRECT, result);
        verify(redirectAttributes).addFlashAttribute("signupSuccess", true);
        verify(userService).existsByEmail(VALID_EMAIL);
        verifyNoInteractions(model);
    }

    @Test
    void handleSignup_WithDuplicateEmail_ShouldReturnSignupView() {
        SignupForm form = createValidForm();
        when(userService.existsByEmail(VALID_EMAIL)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

        assertEquals(SIGNUP_VIEW, result);
        verify(bindingResult).rejectValue("email", "duplicate", "Email already registered");
        verify(userService).existsByEmail(VALID_EMAIL);
        verifyNoInteractions(redirectAttributes);
    }

    @Test
    void handleSignup_WithMismatchedPasswords_ShouldReturnSignupView() {
        SignupForm form = createFormWithMismatchedPasswords();
        when(userService.existsByEmail(VALID_EMAIL)).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

        assertEquals(SIGNUP_VIEW, result);
        verify(bindingResult).rejectValue("passwordConfirm", "mismatch", "Passwords do not match");
        verify(userService).existsByEmail(VALID_EMAIL);
        verifyNoInteractions(redirectAttributes);
    }

    @Test
    void handleSignup_WithBothEmailAndPasswordErrors_ShouldReturnSignupView() {
        SignupForm form = createFormWithMismatchedPasswords();
        form.setEmail(DUPLICATE_EMAIL);
        when(userService.existsByEmail(DUPLICATE_EMAIL)).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

        assertEquals(SIGNUP_VIEW, result);
        verify(bindingResult).rejectValue("email", "duplicate", "Email already registered");
        verify(bindingResult).rejectValue("passwordConfirm", "mismatch", "Passwords do not match");
        verify(userService).existsByEmail(DUPLICATE_EMAIL);
        verifyNoInteractions(redirectAttributes);
    }

    private SignupForm createValidForm() {
        return createForm("Valid Name", VALID_EMAIL, VALID_PASSWORD, VALID_PASSWORD);
    }

    private SignupForm createFormWithMismatchedPasswords() {
        return createForm("Valid Name", VALID_EMAIL, VALID_PASSWORD, DIFFERENT_PASSWORD);
    }

    private SignupForm createForm(String name, String email, String password, String passwordConfirm) {
        SignupForm form = new SignupForm();
        form.setName(name);
        form.setEmail(email);
        form.setPassword(password);
        form.setPasswordConfirm(passwordConfirm);
        return form;
    }
}