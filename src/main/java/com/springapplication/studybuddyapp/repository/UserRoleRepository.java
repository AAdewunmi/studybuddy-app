package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.model.UserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link UserRole} join entities.
 */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUser_Id(Long userId);

    List<UserRole> findByRole_Name(String roleName); // e.g., "ADMIN" or "USER"

    boolean existsByUser_IdAndRole_Name(Long userId, String roleName);
}

