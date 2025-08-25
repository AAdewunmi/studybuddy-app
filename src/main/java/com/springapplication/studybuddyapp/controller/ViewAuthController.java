package com.springapplication.studybuddyapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders login/signup pages and processes Thymeleaf signup form.
 */
@Controller
public class ViewAuthController {


    @GetMapping({ "/login"})
    public String loginPage() {
        return "login";
    }

}

