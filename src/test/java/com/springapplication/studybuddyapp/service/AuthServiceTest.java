// path: src/test/java/com/springapplication/studybuddyapp/service/AuthServiceTest.java
package com.springapplication.studybuddyapp.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.springapplication.studybuddyapp.api.dto.UserResponse;
import com.springapplication.studybuddyapp.exception.BadRequestException;
import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.exception.NotFoundException;
import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private UserRepository users;
    private RoleRepository roles;
    private UserRoleRepository userRoles;
    private PasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void setup() {
        users = mock(UserRepository.class);
        roles = mock(RoleRepository.class);
        userRoles = mock(UserRoleRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new AuthService(users, roles, userRoles, encoder);
    }

    @Test
    void signup_success_hashesPassword_assignsDefaultRole() {
        when(users.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(encoder.encode("StrongP@ss1")).thenReturn("ENC");
        Role roleUser = new Role("ROLE_USER"); roleUser.setId(1);
        when(roles.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        User saved = new User(); saved.setId(10L); saved.setEmail("alice@example.com"); saved.setName("Alice"); saved.setPasswordHash("ENC");
        when(users.save(any(User.class))).thenReturn(saved);
        when(users.findByIdWithRoles(10L)).thenReturn(Optional.of(saved)); // response mapping

        UserResponse res = service.signup("Alice", "alice@example.com", "StrongP@ss1");

        assertThat(res.getEmail()).isEqualTo("alice@example.com");
        verify(encoder).encode("StrongP@ss1");
        // captured link creation
        ArgumentCaptor<com.springapplication.studybuddyapp.model.UserRole> link = ArgumentCaptor.forClass(com.springapplication.studybuddyapp.model.UserRole.class);
        verify(userRoles).save(link.capture());
        assertThat(link.getValue().getRole().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void signup_duplicateEmail_conflict() {
        when(users.existsByEmailIgnoreCase("dupe@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.signup("Bob", "dupe@example.com", "StrongP@ss1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void signup_weakPassword_badRequest() {
        when(users.existsByEmailIgnoreCase("weak@example.com")).thenReturn(false);
        assertThatThrownBy(() -> service.signup("Weak", "weak@example.com", "weak")) // not strong
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void signup_missingDefaultRole_notFound() {
        when(users.existsByEmailIgnoreCase("x@example.com")).thenReturn(false);
        when(encoder.encode("StrongP@ss1")).thenReturn("ENC");
        when(roles.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roles.findByName("USER")).thenReturn(Optional.empty());
        User saved = new User(); saved.setId(99L);
        when(users.save(any(User.class))).thenReturn(saved);
        assertThatThrownBy(() -> service.signup("X", "x@example.com", "StrongP@ss1"))
                .isInstanceOf(NotFoundException.class);
    }
}

