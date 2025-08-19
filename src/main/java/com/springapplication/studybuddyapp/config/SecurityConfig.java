package com.springapplication.studybuddyapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    // ---- Password encoder (BCrypt) ----
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 10–12 is a good cost for dev; increase for prod if CPU budget allows
        return new BCryptPasswordEncoder(12);
    }

    // ---- Security filter chain (no WebSecurityConfigurerAdapter in Spring Security 6) ----
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/img/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/auth/login")
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}

