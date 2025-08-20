// path: src/test/java/com/springapplication/studybuddyapp/api/AuthControllerWebTest.java
package com.springapplication.studybuddyapp.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.springapplication.studybuddyapp.controller.AuthController;
import com.springapplication.studybuddyapp.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
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
 * Web-layer tests for /auth/login and /logout using a test-only security chain:
 * - /auth/** permitted
 * - CSRF ignored for /auth/** and /logout
 * - logout returns 200 + {"message":"Logout successful"}
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(AuthControllerWebTest.TestSecurityConfig.class)
class AuthControllerWebTest {

    @Autowired MockMvc mvc;

    @MockBean AuthService authService;               // Controller ctor dep
    @MockBean AuthenticationManager authenticationManager;

    @Test
    void login_createsSession_and_logoutReturnsJson() throws Exception {
        Authentication auth =
                new UsernamePasswordAuthenticationToken("alice@example.com", "x", java.util.Collections.emptyList());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        MvcResult login = mvc.perform(post("/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    { "email":"alice@example.com", "password":"SecretP@ss1" }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        HttpSession session = login.getRequest().getSession(false);
        org.assertj.core.api.Assertions.assertThat(session).isNotNull();
        Object ctxAttr = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        org.assertj.core.api.Assertions.assertThat(ctxAttr).isNotNull();

        mvc.perform(post("/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"message\":\"Logout successful\"}"));
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

    /** Test-only minimal security chain mirroring production behavior needed for these tests. */
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
                            .logoutSuccessHandler((req, res, auth) -> {
                                res.setStatus(HttpServletResponse.SC_OK);
                                res.setContentType("application/json");
                                res.getWriter().write("{\"message\":\"Logout successful\"}");
                            })
                    )
                    .formLogin(Customizer.withDefaults());
            return http.build();
        }

        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
        @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    }
}
