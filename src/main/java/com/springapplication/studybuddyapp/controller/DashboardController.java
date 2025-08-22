package com.springapplication.studybuddyapp.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the dashboard for authenticated users.
 */
@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("displayName", principal != null ? principal.getName() : "User");
        // you can add stats/metrics later
        return "dashboard";
    }
}

