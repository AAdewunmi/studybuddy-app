package com.springapplication.studybuddyapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Join entity between {@link User} and {@link Role}.
 *
 * <p>Maps the {@code user_roles} table with a composite primary key
 * ({@link UserRoleId}). This approach makes it easy to add metadata
 * (e.g., assignedAt) later.</p>
 */
@Entity
@Table(name = "user_roles")
public class UserRole {

    /** Composite primary key: (user_id, role_id). */
    @EmbeddedId
    private UserRoleId id;

    /** Owning side to User. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId") // maps this relation to the userId in the embedded id
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Owning side to Role. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId") // maps this relation to the roleId in the embedded id
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Optional metadata (example). */
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    // -------------------- Constructors --------------------

    public UserRole() {}

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleId(user.getId(), role.getId());
    }

    // -------------------- Equality by composite key --------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole)) return false;
        UserRole other = (UserRole) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // -------------------- Getters / Setters --------------------

    public UserRoleId getId() { return id; }
    public void setId(UserRoleId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (this.id == null) this.id = new UserRoleId();
        this.id.setUserId(user != null ? user.getId() : null);
    }

    public Role getRole() { return role; }
    public void setRole(Role role) {
        this.role = role;
        if (this.id == null) this.id = new UserRoleId();
        this.id.setRoleId(role != null ? role.getId() : null);
    }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}

