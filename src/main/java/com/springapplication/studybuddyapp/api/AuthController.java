// path: src/main/java/com/springapplication/studybuddyapp/api/AuthController.java
package com.springapplication.studybuddyapp.api;

import com.springapplication.studybuddyapp.api.dto.SignupRequest;
import com.springapplication.studybuddyapp.api.dto.UserResponse;
import com.springapplication.studybuddyapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints (signup).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    /**
     * POST /auth/signup
     * Creates a user, hashes password, assigns default role (ROLE_USER).
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest req) {
        return authService.signup(req.getUsername(), req.getEmail(), req.getPassword());
    }
}

