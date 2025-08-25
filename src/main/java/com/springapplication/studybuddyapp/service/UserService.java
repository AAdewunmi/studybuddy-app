package com.springapplication.studybuddyapp.service;

import com.springapplication.studybuddyapp.exception.BadRequestException;
import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.exception.NotFoundException;
import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.repository.RoleRepository;
import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UserService handles user lifecycle: create, read, update, delete,
 * change password, and role management.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Create a new user with default role USER. */
    public User createUser(String name, String email, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already in use: " + email);
        }
        if (rawPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u = userRepository.save(u);

        Role userRole = roleRepository.findByName("ROLE_USER")
                // fallback in case older data has USER without prefix
                .orElseGet(() -> roleRepository.findByName("USER")
                        .orElseThrow(() -> new NotFoundException("Default role ROLE_USER not found")));
        userRoleRepository.save(new UserRole(u, userRole));

        return u;
    }

    /**
     * Registers a new user after normalizing input.
     * <p>
     * This method:
     * <ul>
     *   <li>Trims leading/trailing whitespace from {@code email}</li>
     *   <li>Lowercases {@code email}</li>
     *   <li>Delegates to {@link #createUser(String, String, String)} for all
     *       validations (password length), uniqueness checks, persistence,
     *       and default role assignment</li>
     * </ul>
     * Behavior is otherwise identical to {@code createUser}.
     *
     * @param name        display name to store
     * @param email       email to normalize, check for uniqueness and persist
     * @param rawPassword raw password (must be at least 8 chars; checked in {@code createUser})
     * @return the persisted {@link User}
     * @throws ConflictException   if email already exists (case-insensitive)
     * @throws BadRequestException if password is too short
     * @throws NotFoundException   if default role cannot be found
     */
    public User register(String name, String email, String rawPassword) {
        String normalizedEmail = (email == null) ? null : email.trim().toLowerCase();
        return createUser(name, normalizedEmail, rawPassword);
    }

    /** Get by id or 404. */
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    /** List all users (lightweight). */
    public java.util.List<User> listUsers() {
        return userRepository.findAll();
    }

    /** Update name and/or email. */
    public User updateProfile(Long id, String name, String email) {
        User u = getUser(id);

        if (!u.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already in use: " + email);
        }
        u.setName(name);
        u.setEmail(email);
        return userRepository.save(u);
    }

    /** Change password with current password verification. */
    public void changePassword(Long id, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters");
        }
        User u = getUser(id);
        if (!passwordEncoder.matches(currentPassword, u.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(u);
    }

    /** Add role to user if absent (e.g., ADMIN). */
    public void addRole(Long id, String roleName) {
        User u = getUser(id);
        Role r = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        if (!userRoleRepository.existsByUser_IdAndRole_Name(u.getId(), r.getName())) {
            userRoleRepository.save(new UserRole(u, r));
        }
    }

    /** Remove role if present. */
    public void removeRole(Long id, String roleName) {
        User u = getUser(id);
        java.util.List<com.springapplication.studybuddyapp.model.UserRole> links =
                userRoleRepository.findByUser_Id(u.getId());
        links.stream()
                .filter(ur -> ur.getRole().getName().equals(roleName))
                .forEach(userRoleRepository::delete);
    }

    /** Delete user and cascade remove links. */
    public void deleteUser(Long id) {
        User u = getUser(id);
        userRepository.delete(u);
    }

    /** Utility: return role names for a user. */
    public Set<String> roleNames(User u) {
        return u.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    /**
     * Find a user by their display name ("username") or 404.
     *
     * @param name display name to look up (must not be blank)
     * @return the matching {@link User}
     * @throws BadRequestException if name is null/blank
     * @throws NotFoundException if no user with that name exists
     */
    public User findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name must be provided");
        }
        return userRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("User not found by name: " + name));
    }
}


