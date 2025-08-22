package com.springapplication.studybuddyapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Shows a logout confirmation page with a POST form to /logout.
 * GET /logout renders the page; POST /logout is handled by Spring Security.
 */
@Controller
public class LogoutPageController {

    @GetMapping("/logout")
    public String logoutPage() {
        return "logout";
    }
}

