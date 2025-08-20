// path: src/main/java/com/springapplication/studybuddyapp/api/AuthController.java
package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.api.dto.LoginRequest;
import com.springapplication.studybuddyapp.api.dto.SignupRequest;
import com.springapplication.studybuddyapp.api.dto.UserResponse;
import com.springapplication.studybuddyapp.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints (signup + login).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * POST /auth/signup
     * Creates a user, hashes password, assigns default role (ROLE_USER/USER).
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest req) {
        return authService.signup(req.getUsername(), req.getEmail(), req.getPassword());
    }

    /**
     * POST /auth/login
     * Authenticates user credentials via Spring Security.
     * Returns 200 with a simple message if authentication succeeds.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        // store the authentication in SecurityContext for this request (session/stateless handling can be added later)
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }
}


