// path: src/main/java/com/springapplication/studybuddyapp/security/SecurityConfig.java
package com.springapplication.studybuddyapp.config;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Production security setup for StudyBuddy:
 * 1) Permits /auth/** (signup/login JSON endpoints)
 * 2) Secures everything else
 * 3) Uses session-based authentication (default)
 * 4) Custom logout success: 200 OK + {"message":"Logout successful"}
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
                // CSRF: ignore for JSON auth endpoints and logout so curl/clients can call them easily
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/logout"))

                // Sessions (default is IF_REQUIRED; we set it explicitly for clarity)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // DB-backed authentication provider (requires UserDetailsService + PasswordEncoder beans)
                .authenticationProvider(daoAuthenticationProvider)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()   // permit signup/login
                        .requestMatchers("/", "/assets/**", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()              // everything else
                )

                // HTTP basic + form (handy during dev; keep or remove later)
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login").permitAll().defaultSuccessUrl("/dashboard", true))

                // Custom logout: 200 + JSON body
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(HttpServletResponse.SC_OK);
                            res.setContentType("application/json");
                            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            res.getWriter().write("{\"message\":\"Logout successful\"}");
                        })
                );

        return http.build();
    }
}

