// src/main/java/com/springapplication/studybuddyapp/controller/HomeController.java
package com.springapplication.studybuddyapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "index"; // Make sure you have src/main/resources/templates/index.html
    }
}

