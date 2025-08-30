package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.service.UserServiceInterface;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping(path = "/signup")
    public String handleSignup(@ModelAttribute @Valid SignupForm signupForm,
                               BindingResult bindingResult,
                               RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            // Return the user to the signup form with validation errors
            return "signup";
        }

        String password = signupForm.getPassword();
        String passwordConfirm = signupForm.getPasswordConfirm();
        if (!password.equals(passwordConfirm)) {
            return "redirect:/signup?mismatch";
        }

        userService.register(signupForm.getName(), signupForm.getEmail(), password);
        ra.addFlashAttribute("signupSuccess", true);
        return "redirect:/login?registered";
    }
}
