// path: src/test/java/com/springapplication/studybuddyapp/service/UserServiceTest.java
package com.springapplication.studybuddyapp.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.springapplication.studybuddyapp.exception.BadRequestException;
import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.exception.NotFoundException;
import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService(userRepository, roleRepository, userRoleRepository, passwordEncoder);
    }

    @Test
    void createUser_success_assignsUSER() {
        // given
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        // service will first ask for ROLE_USER, return present:
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role("ROLE_USER")));
        // (fallback "USER" will not be used in this path)
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(123L);
            return u;
        });

        // when
        User created = userService.createUser("Alice", "alice@example.com", "Str0ngP@ss!");

        // then
        assertThat(created.getId()).isEqualTo(123L);
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void createUser_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmailIgnoreCase("a@x.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser("A", "a@x.com", "Password123"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void changePassword_wrongCurrent_throws() {
        User u = new User();
        u.setId(10L);
        u.setPasswordHash("ENC");
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "ENC")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(10L, "bad", "NewPass123"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addRole_missingRole_throwsNotFound() {
        User u = new User();
        u.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addRole(10L, "ADMIN"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByName_returnsUser_whenPresent() {
        User u = new User();
        u.setId(42L);
        u.setName("Alice");
        when(userRepository.findByName("Alice")).thenReturn(Optional.of(u));

        User found = userService.findByName("Alice");

        assertThat(found.getId()).isEqualTo(42L);
        verify(userRepository).findByName("Alice");
    }

    @Test
    void findByName_throwsNotFound_whenAbsent() {
        when(userRepository.findByName("Bob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByName("Bob"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found by name: Bob");
    }

    @Test
    void findByName_throwsBadRequest_whenBlank() {
        assertThatThrownBy(() -> userService.findByName("  "))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> userService.findByName(null))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void createUser_throws_ifRoleMissing() {
        when(userRepository.existsByEmailIgnoreCase("no-role@example.com")).thenReturn(false);
        // neither ROLE_USER nor USER present
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class,
                () -> userService.createUser("Bob", "no-role@example.com", "Str0ngP@ss!"));
    }
}

