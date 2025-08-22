package com.springapplication.studybuddyapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**")) // JSON API convenience
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(daoAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup",
                                "/assets/**", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()          // JSON signup/login still public
                        .requestMatchers("/dashboard", "/groups/**").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")                       // form uses email
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")                             // POST /logout
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }
}

