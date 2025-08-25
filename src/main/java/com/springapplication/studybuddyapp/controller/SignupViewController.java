package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.controller.SignupForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class SignupViewController {

    @GetMapping("/signup")
    public String showForm(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping(path = "/signup")
    public String submit(@Valid @ModelAttribute("signupForm") SignupForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Cross-field check
        if (form.getPassword() != null && form.getPasswordConfirm() != null
                && !form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.addError(new FieldError(
                    "signupForm", "passwordConfirm", "Passwords do not match"));
        }

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        // TODO: create user
        redirectAttributes.addFlashAttribute("flashSuccess", "Account created. You can log in.");
        return "redirect:/login?signup=success";
    }
}
