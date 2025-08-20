// path: src/test/java/com/springapplication/studybuddyapp/api/AuthControllerWebTest.java
package com.springapplication.studybuddyapp.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.springapplication.studybuddyapp.controller.AuthController;
import com.springapplication.studybuddyapp.service.AuthService;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Web-layer unit tests for /auth/login and /logout with a test-only SecurityFilterChain:
 * - /auth/** is permitted
 * - CSRF ignored for /auth/** and /logout (dev-friendly)
 *
 * We assert the session + SecurityContext rather than a Set-Cookie header,
 * because MockMvc may not emit Set-Cookie in this slice even when a session exists.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(AuthControllerWebTest.TestSecurityConfig.class)
class AuthControllerWebTest {

    @Autowired MockMvc mvc;

    @MockBean AuthService authService; // controller dependency (not used in login tests)
    @MockBean AuthenticationManager authenticationManager;

    @Test
    void login_createsSessionWithSecurityContext_and_logoutWorks() throws Exception {
        // Mock successful authentication
        Authentication auth =
                new UsernamePasswordAuthenticationToken("alice@example.com", "x", java.util.Collections.emptyList());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        // Perform login
        MvcResult loginResult = mvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email":"alice@example.com", "password":"SecretP@ss1" }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        // ✅ Assert session exists and contains the SecurityContext
        HttpSession session = loginResult.getRequest().getSession(false);
        org.assertj.core.api.Assertions.assertThat(session).as("session should be created").isNotNull();
        Object ctxAttr = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        org.assertj.core.api.Assertions.assertThat(ctxAttr).as("SPRING_SECURITY_CONTEXT present in session").isNotNull();

        // Logout with the same session
        mvc.perform(post("/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk()); // 200 per TestSecurityConfig's logoutSuccessHandler
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

    /** Minimal security for tests: allow /auth/** and /logout; ignore CSRF; logout returns 200. */
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/logout"))
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers("/auth/**", "/logout").permitAll()
                            .anyRequest().denyAll()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessHandler((req, res, auth) -> res.setStatus(200)) // 200 OK on logout
                    )
                    .formLogin(Customizer.withDefaults());
            return http.build();
        }

        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

        @Bean SecurityContextRepository securityContextRepository() {
            return new HttpSessionSecurityContextRepository();
        }
    }
}

