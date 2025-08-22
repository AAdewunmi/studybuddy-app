// path: src/test/java/com/springapplication/studybuddyapp/repository/UserRepositoryFindByNameIT.java
package com.springapplication.studybuddyapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.springapplication.studybuddyapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for UserRepository.findByName using a real Postgres container.
 * Uses a BCrypt-encoded password to satisfy @Size constraints on passwordHash.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryFindByNameIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:15");

    @Autowired UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void findByName_persistsAndFinds() {
        User u = new User();
        u.setName("Charlie");
        u.setEmail("charlie@example.com");
        // BCrypt hash is 60 chars → always satisfies @Size(min=8)
        u.setPasswordHash(encoder.encode("Str0ngP@ss!"));
        userRepository.saveAndFlush(u);

        assertThat(userRepository.findByName("Charlie")).isPresent();

        // If you later switch to ignore-case, update this expectation accordingly
        assertThat(userRepository.findByName("charlie")).isNotPresent();
    }
}


