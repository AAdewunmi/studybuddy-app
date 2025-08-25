package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.service.UserServiceInterface;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ViewAuthController {

    private final UserServiceInterface userService;

    public ViewAuthController(UserServiceInterface userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@Valid @ModelAttribute("signupForm") SignupForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Check for duplicate email
        if (userService.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Email already registered");
        }

        // Cross-field password validation
        if (form.getPassword() != null && form.getPasswordConfirm() != null
                && !form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.addError(new FieldError(
                    "signupForm", "passwordConfirm", "Passwords do not match"));
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        // TODO: perform actual registration using your existing service
        // e.g. userService.register(form);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/login?registered";
    }
}