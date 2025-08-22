package com.springapplication.studybuddyapp.controller;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Groups page (placeholder list). Replace with real GroupService when ready.
 */
@Controller
public class GroupsController {

    @GetMapping("/groups")
    public String groups(Model model) {
        // Placeholder data until Group / GroupService exists
        List<Map<String, String>> groups = List.of(
                Map.of("name", "Algorithms Study Group", "role", "ADMIN"),
                Map.of("name", "Spring Boot Learners", "role", "MEMBER")
        );
        model.addAttribute("groups", groups);
        return "groups";
    }
}

