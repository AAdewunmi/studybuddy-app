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
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class CustomUserDetailsServiceTest {

    @Test
    void loadUserByUsername_mapsRolesWithPrefix() {
        var repo = mock(UserRepository.class);
        var service = new CustomUserDetailsService(repo);

        var user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$10$fakehash");
        var roleUser = new Role("USER"); roleUser.setId(1);
        user.getUserRoles().add(new UserRole(user, roleUser));

        when(repo.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));

        UserDetails ud = service.loadUserByUsername("alice@example.com");
        assertThat(ud.getUsername()).isEqualTo("alice@example.com");
        assertThat(ud.getAuthorities()).extracting("authority")
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
}

