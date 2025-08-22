package com.springapplication.studybuddyapp.bootstrap;

import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds an administrator account with ROLE_ADMIN and ROLE_USER if it doesn't exist.
 * Idempotent: safe to run on every startup.
 */
@Configuration
public class AdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    @Bean
    @Profile("!test") // avoid interfering with test seeding
    CommandLineRunner seedAdminRunner(
            @Value("${app.seed.admin.enabled:true}") boolean enabled,
            @Value("${app.admin.name:Administrator}") String adminName,
            @Value("${app.admin.email:admin@studybuddy.local}") String adminEmail,
            @Value("${app.admin.password:ChangeMe123!}") String adminPassword,
            RoleRepository roles,
            UserRepository users,
            UserRoleRepository userRoles,
            PasswordEncoder encoder
    ) {
        return args -> {
            if (!enabled) {
                log.info("Admin seeding is disabled (app.seed.admin.enabled=false).");
                return;
            }

            // Ensure roles exist
            Role roleUser = roles.findByName("ROLE_USER").orElseGet(() -> {
                log.info("Creating role ROLE_USER");
                return roles.save(new Role("ROLE_USER"));
            });
            Role roleAdmin = roles.findByName("ROLE_ADMIN").orElseGet(() -> {
                log.info("Creating role ROLE_ADMIN");
                return roles.save(new Role("ROLE_ADMIN"));
            });

            // Ensure admin user exists
            User admin = users.findByEmailIgnoreCase(adminEmail).orElseGet(() -> {
                log.info("Creating admin user {}", adminEmail);
                User u = new User();
                u.setName(adminName);
                u.setEmail(adminEmail);
                u.setPasswordHash(encoder.encode(adminPassword)); // BCrypt
                return users.save(u);
            });

            // Ensure role links exist
            if (!userRoles.existsByUser_IdAndRole_Name(admin.getId(), "ROLE_USER")) {
                log.info("Granting ROLE_USER to {}", adminEmail);
                userRoles.save(new UserRole(admin, roleUser));
            }
            if (!userRoles.existsByUser_IdAndRole_Name(admin.getId(), "ROLE_ADMIN")) {
                log.info("Granting ROLE_ADMIN to {}", adminEmail);
                userRoles.save(new UserRole(admin, roleAdmin));
            }

            log.info("Admin seeding completed: {}", adminEmail);
        };
    }
}

