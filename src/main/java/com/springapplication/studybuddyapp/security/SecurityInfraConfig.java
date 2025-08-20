// path: src/main/java/com/springapplication/studybuddyapp/security/SecurityInfraConfig.java
package com.springapplication.studybuddyapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Infrastructure bean that persists the authenticated SecurityContext
 * into the HTTP session (used by /auth/login controller).
 */
@Configuration
public class SecurityInfraConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}

