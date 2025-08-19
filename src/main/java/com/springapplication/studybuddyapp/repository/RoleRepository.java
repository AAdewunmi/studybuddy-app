package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Role}.
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}

