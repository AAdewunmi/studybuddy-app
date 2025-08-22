// path: src/main/java/com/springapplication/studybuddyapp/repository/UserRepository.java
package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link User}.
 *
 * <p>Note on naming: in this project the "username" domain concept maps to the
 * {@code User.name} column. To keep call sites expressive, this repository exposes
 * {@link #findByUsername(String)} and delegates to a Spring Data derived query on
 * {@code name}, plus a fetch-join variant to eagerly load roles when needed.</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // ----- existing methods -----
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
           select u from User u
           left join fetch u.userRoles ur
           left join fetch ur.role r
           where u.id = :id
           """)
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    @Query("""
           select distinct u from User u
           left join fetch u.userRoles ur
           left join fetch ur.role r
           """)
    List<User> findAllWithRoles();

    // ----- NEW: username lookups -----

    /**
     * Case-insensitive lookup by domain "username", which maps to the {@code name} field.
     */
    Optional<User> findByNameIgnoreCase(String username);

    /**
     * Friendly alias for {@link #findByNameIgnoreCase(String)} so call sites can use
     * domain language ("username") without knowing the underlying column name.
     */
    default Optional<User> findByUsername(String username) {
        return findByNameIgnoreCase(username);
    }

    /**
     * Lookup by username (name) and eagerly fetch associated roles to avoid
     * lazy-loading outside a transaction.
     */
    @Query("""
           select u from User u
           left join fetch u.userRoles ur
           left join fetch ur.role r
           where lower(u.name) = lower(:username)
           """)
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}
