package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.controller.SignupForm;
import com.springapplication.studybuddyapp.controller.SignupViewController;
import com.springapplication.studybuddyapp.service.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SignupViewControllerTest {

    private static final String VALID_PASSWORD = "Password1!";
    private static final String DIFFERENT_PASSWORD = "Password2!";
    private static final String SIGNUP_VIEW = "signup";


    private SignupViewController signupViewController;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        signupViewController = new SignupViewController();
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ViewAuthControllerTest {

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
        void testHandleSignup_SuccessfulRegistration() {
            SignupForm form = createValidForm();
            when(userService.existsByEmail(form.getEmail())).thenReturn(false);
            when(bindingResult.hasErrors()).thenReturn(false);

            String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

            assertEquals("redirect:/login?registered", result);
            verify(redirectAttributes).addFlashAttribute("signupSuccess", true);
            verifyNoInteractions(model);
        }

        @Test
        void testHandleSignup_EmailAlreadyExists() {
            SignupForm form = createValidForm();
            when(userService.existsByEmail(form.getEmail())).thenReturn(true);
            when(bindingResult.hasErrors()).thenReturn(true);

            String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

            assertEquals("auth/signup", result);
            verify(bindingResult).rejectValue("email", "duplicate", "Email already registered");
            verifyNoInteractions(redirectAttributes);
        }

        @Test
        void testHandleSignup_PasswordsDoNotMatch() {
            SignupForm form = createFormWithMismatchedPasswords();
            when(userService.existsByEmail(form.getEmail())).thenReturn(false);
            when(bindingResult.hasErrors()).thenReturn(true);

            String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

            assertEquals("auth/signup", result);
            verify(bindingResult).rejectValue("passwordConfirm", "mismatch", "Passwords do not match");
            verifyNoInteractions(redirectAttributes);
        }

        @Test
        void testHandleSignup_BothEmailAndPasswordErrors() {
            SignupForm form = createFormWithMismatchedPasswords();
            when(userService.existsByEmail(form.getEmail())).thenReturn(true);
            when(bindingResult.hasErrors()).thenReturn(true);

            String result = controller.handleSignup(form, bindingResult, model, redirectAttributes);

            assertEquals("auth/signup", result);
            verify(bindingResult).rejectValue("email", "duplicate", "Email already registered");
            verify(bindingResult).rejectValue("passwordConfirm", "mismatch", "Passwords do not match");
            verifyNoInteractions(redirectAttributes);
        }

        private SignupForm createValidForm() {
            SignupForm form = new SignupForm();
            form.setName("Valid Name");
            form.setEmail("valid@example.com");
            form.setPassword("ValidPass123!");
            form.setPasswordConfirm("ValidPass123!");
            return form;
        }

        private SignupForm createFormWithMismatchedPasswords() {
            SignupForm form = new SignupForm();
            form.setName("Valid Name");
            form.setEmail("valid@example.com");
            form.setPassword("Password1!");
            form.setPasswordConfirm("Password2!");
            return form;
        }
    }

    @Test
    void testSubmit_WhenBindingResultHasErrors_ShouldReturnSignupView() {
        SignupForm form = new SignupForm();
        form.setPassword("Password1!");
        form.setPasswordConfirm("Password1!");

        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = signupViewController.submit(form, bindingResult, model, redirectAttributes);

        verifyNoInteractions(redirectAttributes);
        assertEquals("signup", viewName);
    }

    @Test
    void testSubmit_WhenValidForm_ShouldRedirectToLogin() {
        SignupForm form = new SignupForm();
        form.setPassword("Password1!");
        form.setPasswordConfirm("Password1!");

        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = signupViewController.submit(form, bindingResult, model, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("flashSuccess", "Account created. You can log in.");
        assertEquals("redirect:/login?signup=success", viewName);
    }
}