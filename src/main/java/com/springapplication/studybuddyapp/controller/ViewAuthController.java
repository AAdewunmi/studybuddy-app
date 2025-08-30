package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.service.UserServiceInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    @GetMapping("/login")
    public String login() {
        return "login"; // will resolve to login.html in templates/
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@ModelAttribute SignupForm signupForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // Validate form
        if (bindingResult.hasErrors()) {
            return "signup";  // Stay on the signup page if there are validation errors
        }

        try {
            userService.register(signupForm.getName(), signupForm.getEmail(), signupForm.getPassword());
            redirectAttributes.addFlashAttribute("message", "Signup successful!");
            System.out.println("Redirecting to /login");
            return "redirect:/login";  // Ensure redirection to /login
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
            bindingResult.reject("signupError", e.getMessage());
            return "signup";
        }
    }
}
