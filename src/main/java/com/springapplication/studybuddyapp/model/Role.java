package com.springapplication.studybuddyapp.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Security role (e.g., USER, ADMIN).
 *
 * <p>Maps to {@code roles}. Role names are unique. The association to users
 * is represented via {@link UserRole} join entities.</p>
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
        }
)
public class Role {

    /** Primary key (serial). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Unique role name (choose one convention: USER/ADMIN or ROLE_USER/ROLE_ADMIN). */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Back-reference to join entities. */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    // -------------------- Constructors --------------------

    public Role() {}
    public Role(String name) { this.name = name; }

    // -------------------- Equality (by unique name) --------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role other = (Role) o;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    // -------------------- Getters / Setters --------------------

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<UserRole> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<UserRole> userRoles) { this.userRoles = userRoles; }
}

