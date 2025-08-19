package com.springapplication.studybuddyapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Application user entity (normalized schema).
 *
 * <p>This entity maps to the {@code users} table and uses a normalized schema
 * for roles via a join entity {@link UserRole}, allowing additional metadata
 * on the user-role relationship if needed.</p>
 *
 * <p>Each user has a unique email, a display name, and a securely hashed
 * password (e.g., BCrypt). The entity also tracks creation time.</p>
 *
 * @see Role
 * @see UserRole
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class User {

    /** Primary key (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name shown on dashboards, groups, etc. */
    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String name;

    /** Unique email address used for login/identity. */
    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Hashed password (BCrypt or similar). */
    @NotBlank
    @Size(min = 8)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Account creation timestamp (default: now). */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Role links for this user (many-to-many via join entity).
     *
     * <p>The owning side is {@link UserRole}. We use {@code orphanRemoval}
     * to delete dangling links and allow full lifecycle management from
     * the {@code User} side.</p>
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<UserRole> userRoles = new HashSet<>();

    // -------------------- Role accessors --------------------

    /**
     * Read-only view of the user's roles.
     *
     * <p>To modify, use {@link #addRole(Role)} or {@link #removeRole(Role)}.
     * This getter is marked {@code @Transient} so JPA doesn't treat it as a column.</p>
     *
     * @return unmodifiable set of roles
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

    /**
     * Add a role to this user (idempotent).
     * Also maintains reverse relationship on {@link Role#getUserRoles()}.
     *
     * @param role role to assign
     */
    public void addRole(Role role) {
        Objects.requireNonNull(role, "role must not be null");
        UserRole link = new UserRole(this, role);
        userRoles.add(link);
        role.getUserRoles().add(link);
    }

    /**
     * Remove a role from this user (safe if role is not assigned).
     *
     * @param role role to remove
     */
    public void removeRole(Role role) {
        if (role == null) return;
        userRoles.removeIf(ur -> {
            boolean match = ur.getRole().equals(role);
            if (match) {
                role.getUserRoles().remove(ur);
            }
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


