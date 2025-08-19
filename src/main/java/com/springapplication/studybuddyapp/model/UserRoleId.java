package com.springapplication.studybuddyapp.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link UserRole}.
 *
 * <p>Composed of {@code userId} and {@code roleId}, matching the
 * {@code user_roles(user_id, role_id)} primary key.</p>
 */
@Embeddable
public class UserRoleId implements Serializable {

    private Long userId;
    private Integer roleId;

    public UserRoleId() {}

    public UserRoleId(Long userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId)) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}

