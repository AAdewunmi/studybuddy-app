import com.springapplication.studybuddyapp.controller.SignupForm;
import com.springapplication.studybuddyapp.controller.SignupViewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

    @Test
    void testSubmit_WhenPasswordsDoNotMatch_ShouldReturnSignupView() {
        SignupForm form = createFormWithMismatchedPasswords();

        // Mock hasErrors to return true after the password mismatch error is added
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = signupViewController.submit(form, bindingResult, model, redirectAttributes);

        verify(bindingResult).addError(any(FieldError.class));
        verifyNoInteractions(redirectAttributes);
        assertEquals(SIGNUP_VIEW, viewName);
    }




    private SignupForm createFormWithMismatchedPasswords() {
        SignupForm form = new SignupForm();
        form.setPassword(VALID_PASSWORD);
        form.setPasswordConfirm(DIFFERENT_PASSWORD);
        return form;
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