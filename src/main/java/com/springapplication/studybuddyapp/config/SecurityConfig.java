package com.springapplication.studybuddyapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Session-based security:
 * - Public: "/", "/login", "/signup", static assets
 * - Protected: "/dashboard", "/groups/**"
 * - API auth endpoints (/auth/**) still permitted (JSON), with CSRF ignored for convenience.
 * - Form login uses "email" as username parameter.
 * - Logout redirects to "/login?logout" (Thymeleaf-friendly).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {
        http
                // CSRF: forms include token; JSON /auth/** may skip CSRF for convenience
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/logout"))

                // Use our explicit AuthenticationManager (DAO provider + BCrypt)
                .authenticationManager(authenticationManager)

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup",
                                "/assets/**", "/css/**", "/js/**", "/webjars/**").permitAll()
                        // permit POST /login to allow the authentication attempt
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        // JSON auth endpoints:
                        .requestMatchers("/auth/**").permitAll()
                        // protected UI
                        .requestMatchers("/dashboard", "/groups/**").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/login")      // filter listens here
                        .usernameParameter("email")        // <— IMPORTANT: matches your form/tests
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }
}

