// path: src/main/java/com/springapplication/studybuddyapp/config/AuthProvidersConfig.java
package com.springapplication.studybuddyapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Wires the DaoAuthenticationProvider with our CustomUserDetailsService and PasswordEncoder.
 */
@Configuration
public class AuthProvidersConfig {

    private final UserDetailsService userDetailsService; // provided by @Service above
    private final PasswordEncoder passwordEncoder;

    public AuthProvidersConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Authentication provider backed by the DB. */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    /** Exposes AuthenticationManager for login endpoints if you need it. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


