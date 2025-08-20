// path: src/test/java/com/springapplication/studybuddyapp/service/UserServiceIT.java
package com.springapplication.studybuddyapp.service;

import static org.assertj.core.api.Assertions.*;

import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-stack test using a real Postgres DB, managed by Spring Boot via @ServiceConnection.
 * (If you use @DynamicPropertySource instead, keep that approach but the re-load logic remains the same.)
 */
@Testcontainers
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15");

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void seedRoles() {
        if (!roleRepository.existsByName("USER")) roleRepository.save(new Role("USER"));
        if (!roleRepository.existsByName("ADMIN")) roleRepository.save(new Role("ADMIN"));
    }

    @Test
    void createUser_persists_andAssignsUSER() {
        User created = userService.createUser("Carol", "carol@test.com", "Password123!");

        // re-load WITH roles initialized (fetch-join)
        User reloaded = userRepository.findByIdWithRoles(created.getId()).orElseThrow();

        assertThat(created.getId()).isNotNull();
        assertThat(encoder.matches("Password123!", reloaded.getPasswordHash())).isTrue();
        assertThat(userService.roleNames(reloaded)).contains("USER");
    }

    @Test
    void uniqueEmail_enforced() {
        userService.createUser("Bob", "bob@test.com", "Password123!");
        assertThatThrownBy(() -> userService.createUser("Bobby", "bob@test.com", "Password123!"))
                .hasMessageContaining("Email already in use");
    }

    @Test
    void addRole_admin_ok() {
        User created = userService.createUser("Alice", "alice@test.com", "Password123!");

        userService.addRole(created.getId(), "ADMIN");

        // re-load WITH roles initialized (fetch-join)
        User reloaded = userRepository.findByIdWithRoles(created.getId()).orElseThrow();

        assertThat(userService.roleNames(reloaded)).contains("ADMIN", "USER");
    }
}




