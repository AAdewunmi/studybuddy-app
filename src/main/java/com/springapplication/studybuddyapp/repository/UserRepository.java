// path: src/main/java/com/springapplication/studybuddyapp/repository/UserRepository.java
package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.api.dto.SignupForm;
import com.springapplication.studybuddyapp.exception.DuplicateEmailException;
import com.springapplication.studybuddyapp.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNameIgnoreCase(String name);
    /** Derived query for exact name match. */
    Optional<User> findByName(String name);

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

    @Query("""
           select u from User u
           left join fetch u.userRoles ur
           left join fetch ur.role r
           where lower(u.email) = lower(:email)
           """)
    Optional<User> findByEmailIgnoreCaseWithRoles(@Param("email") String email);

    boolean existsByEmail(String email);

}


