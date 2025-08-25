package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.controller.SignupForm; // adjust import if needed
import com.springapplication.studybuddyapp.service.UserServiceInterface;
import jakarta.validation.Valid;
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

    @PostMapping("/signup")
    public String handleSignup(@Valid @ModelAttribute("signupForm") SignupForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (userService.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Email already registered");
        }
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup"; // 200 OK with field errors
        }

        // TODO: perform actual registration using your existing service
        // e.g. userService.register(form);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/login?registered";
    }
}


