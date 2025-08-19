package com.springapplication.studybuddyapp.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Application user (normalized schema).
 *
 * <p>Maps to the {@code users} table and does NOT contain a "role" column.
 * Roles are associated via the join table {@code user_roles} using
 * the {@link UserRole} entity below.</p>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class User {

    /** Primary key (serial). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name. */
    @Column(nullable = false)
    private String name;

    /** Unique email (used for login / identity). */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt (or similar) hashed password. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Account creation timestamp. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * User-role links (owned side is UserRole with composite key).
     *
     * <p>We keep the association on the join entity to allow additional
     * attributes in the future (e.g., assignedAt). If you prefer a plain
     * ManyToMany, you can map {@code Set<Role>} via {@code @ManyToMany} with
     * {@code @JoinTable} — but the join-entity approach is more flexible.</p>
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<UserRole> userRoles = new HashSet<>();

    // -------------------- Convenience accessors --------------------

    /**
     * Read-only view of the user's roles.
     * Use {@link #addRole(Role)} / {@link #removeRole(Role)} to modify.
     */
    @Transient
    public Set<Role> getRoles() {
        if (userRoles == null || userRoles.isEmpty()) return Collections.emptySet();
        Set<Role> roles = new HashSet<>();
        for (UserRole ur : userRoles) {
            roles.add(ur.getRole());
        }
        return Collections.unmodifiableSet(roles);
    }

    /** Attach a role to this user (idempotent). */
    public void addRole(Role role) {
        Objects.requireNonNull(role, "role must not be null");
        UserRole link = new UserRole(this, role);
        userRoles.add(link);
        role.getUserRoles().add(link);
    }

    /** Detach a role from this user (safe if absent). */
    public void removeRole(Role role) {
        if (role == null) return;
        userRoles.removeIf(ur -> {
            boolean match = ur.getRole().equals(role);
            if (match) role.getUserRoles().remove(ur);
            return match;
        });
    }

    // -------------------- Getters / Setters --------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<UserRole> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<UserRole> userRoles) { this.userRoles = userRoles; }
}

