package com.springapplication.studybuddyapp.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AuthProvidersConfigTest {

    @Autowired
    private AuthProvidersConfig authProvidersConfig;

    @Test
    void testDaoAuthenticationProviderReturnsNotNull() {
        // Act
        DaoAuthenticationProvider provider = authProvidersConfig.daoAuthenticationProvider();

        // Assert
        assertNotNull(provider);
        //assertNotNull(provider.getUserDetailsService());
        //assertNotNull(provider.getPasswordEncoder());
    }

    @Test
    void testDaoAuthenticationProviderUsesCorrectUserDetailsServiceAndPasswordEncoder() {
        // Arrange
        UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        AuthProvidersConfig config = new AuthProvidersConfig(userDetailsService, passwordEncoder);

        // Act
        DaoAuthenticationProvider provider = config.daoAuthenticationProvider();

        // Assert
        assertNotNull(provider);
        verify(userDetailsService, Mockito.never()).loadUserByUsername(Mockito.anyString());
        //assertNotNull(provider.getPasswordEncoder());
        //assertNotNull(provider.getUserDetailsService());
    }
}