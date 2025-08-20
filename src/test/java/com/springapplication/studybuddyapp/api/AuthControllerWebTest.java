// path: src/test/java/com/springapplication/studybuddyapp/api/AuthControllerWebTest.java
package com.springapplication.studybuddyapp.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.springapplication.studybuddyapp.controller.AuthController;
import com.springapplication.studybuddyapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer unit tests for /auth/login with a test-only SecurityFilterChain:
 * - /auth/** is permitted
 * - CSRF is ignored for /auth/** so POST works without a token (but we include one anyway for clarity)
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true) // include Spring Security filters
@Import(AuthControllerWebTest.TestSecurityConfig.class)
class AuthControllerWebTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AuthService authService; // controller constructor dependency (not used by /auth/login tests)

    @MockBean
    AuthenticationManager authenticationManager;

    @Test
    void login_success_returns200() throws Exception {
        Authentication auth =
                new UsernamePasswordAuthenticationToken("alice@example.com", "x", java.util.Collections.emptyList());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        mvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())       // ok even though CSRF is ignored for /auth/**
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email":"alice@example.com", "password":"SecretP@ss1" }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email":"alice@example.com", "password":"wrong" }
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    /** Minimal, test-only security config for this web slice. */
    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**")) // ignore CSRF for auth endpoints in tests
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/auth/**").permitAll()             // allow hitting /auth/login
                            .anyRequest().denyAll()
                    )
                    .formLogin(Customizer.withDefaults());
            return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
