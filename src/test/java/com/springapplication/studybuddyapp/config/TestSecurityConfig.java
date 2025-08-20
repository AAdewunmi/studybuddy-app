// path: src/test/java/com/springapplication/studybuddyapp/config/TestSecurityConfig.java
package com.springapplication.studybuddyapp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-only minimal security config for @WebMvcTest.
 * - /dashboard/** requires auth
 * - CSRF enabled by default
 * - default login page for 302 redirects
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/dashboard/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(Customizer.withDefaults()); // provides /login for redirects
        // CSRF remains enabled by default
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


