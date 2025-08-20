// path: src/test/java/com/springapplication/studybuddyapp/security/SecurityContextIT.java
package com.springapplication.studybuddyapp.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class SecurityContextIT {

    @Autowired UserDetailsService uds;
    @Autowired PasswordEncoder encoder;
    @Autowired AuthenticationManager authenticationManager;

    @Test
    void beansPresent() {
        assertThat(uds).isNotNull();
        assertThat(encoder).isNotNull();
        assertThat(authenticationManager).isNotNull();
    }
}

