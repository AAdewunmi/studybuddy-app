// path: src/main/java/com/springapplication/studybuddyapp/security/SecurityInfraConfig.java
package com.springapplication.studybuddyapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Provides infrastructure beans used by authentication endpoints.
 * In particular, a SecurityContextRepository that persists authentication
 * into the HTTP session so subsequent requests are authenticated.
 */
@Configuration
public class SecurityInfraConfig {

    /** Stores/retrieves SecurityContext in/from the HttpSession. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
