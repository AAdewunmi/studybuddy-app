package com.springapplication.studybuddyapp.service;

import com.springapplication.studybuddyapp.exception.BadRequestException;
import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
/**
 * Integration tests for UserService register.
 */
class UserServiceRegisterTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        service = new UserService(userRepository, roleRepository, userRoleRepository, passwordEncoder);

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role("ROLE_USER")));
    }

    @Test
    void register_trims_and_lowercases_email_then_delegates() {
        // given
        String name = "Ada";
        String emailWithSpacesAndCaps = "  ADA@Example.COM  ";
        String expectedEmail = "ada@example.com";
        String password = "SecretP@ss1";

        when(userRepository.existsByEmailIgnoreCase(expectedEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(42L);
            return u;
        });

        // when
        User created = service.register(name, emailWithSpacesAndCaps, password);

        // then
        assertThat(created.getId()).isEqualTo(42L);

        // Capture saved entity to inspect normalized email & encoded password
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertThat(saved.getValue().getEmail()).isEqualTo(expectedEmail);
        assertThat(passwordEncoder.matches(password, saved.getValue().getPasswordHash())).isTrue();

        // role link created
        verify(userRoleRepository, times(1)).save(any());
    }

    @Test
    void register_preserves_password_length_rule() {
        // given
        String shortPwd = "short";
        // when/then
        assertThatThrownBy(() -> service.register("Bob", "bob@example.com", shortPwd))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 8");
        verify(userRepository, never()).save(any(User.class));
        verify(userRoleRepository, never()).save(any());
        verify(roleRepository, never()).findByName(anyString());

    }

    @Test
    void register_preserves_duplicate_email_check() {
        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register("Jane", "  Jane@Example.com  ", "ValidPass1!"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");

        // ensure no save attempt
        verify(userRepository, never()).save(any(User.class));
    }
}

