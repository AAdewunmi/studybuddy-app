// path: src/test/java/com/springapplication/studybuddyapp/security/CustomUserDetailsServiceTest.java
package com.springapplication.studybuddyapp.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.UserRepository;
import java.util.Optional;

import com.springapplication.studybuddyapp.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class CustomUserDetailsServiceTest {
    @Mock
    UserRepository users;
    @InjectMocks
    CustomUserDetailsService uds;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void loadUserByUsername_mapsRolesWithPrefix() {
        String email = "alice@example.com";
        // Build an entity user with ROLE_USER
        User u = new User();
        u.setId(1L);
        u.setName("Alice");
        u.setEmail(email);
        u.setPasswordHash("{bcrypt}xxx");
        Role r = new Role("ROLE_USER");
        u.getUserRoles().add(new UserRole(u, r));

        // IMPORTANT: mock the eager method that the service calls
        when(users.findByEmailIgnoreCaseWithRoles(email)).thenReturn(Optional.of(u));

        UserDetails details = uds.loadUserByUsername(email);
        assertThat(details.getUsername()).isEqualTo(email);
        assertThat(details.getAuthorities().stream().map(a -> a.getAuthority()))
                .contains("ROLE_USER");
    }

    @Test
    void loadUserByUsername_missing_throws() {
        var repo = mock(UserRepository.class);
        when(repo.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        var service = new CustomUserDetailsService(repo);

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_userNotFound_throws() {
        when(users.findByEmailIgnoreCaseWithRoles("missing@example.com")).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(
                UsernameNotFoundException.class,
                () -> uds.loadUserByUsername("missing@example.com"));
    }
}

