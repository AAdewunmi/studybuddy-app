package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.service.UserService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Renders login/signup pages and processes Thymeleaf signup form.
 */
@Controller
public class ViewAuthController {

    private final UserService userService;
    private final com.springapplication.studybuddyapp.repository.UserRepository userRepository;

    public ViewAuthController(UserService userService,
                              com.springapplication.studybuddyapp.repository.UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@Valid @ModelAttribute("signupForm") SignupForm form,
                               BindingResult binding, Model model) {

        // Basic UI validations
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            binding.rejectValue("email", "email.exists", "Email already in use");
        }
        if (userRepository.existsByNameIgnoreCase(form.getName())) {
            binding.rejectValue("name", "name.exists", "Username already in use");
        }
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            binding.rejectValue("passwordConfirm", "password.mismatch", "Passwords do not match");
        }
        if (binding.hasErrors()) {
            return "signup";
        }

        // Create via service (hashes password, assigns default role)
        userService.createUser(form.getName(), form.getEmail(), form.getPassword());
        // Redirect to login with success flag
        return "redirect:/login?registered";
    }
}

