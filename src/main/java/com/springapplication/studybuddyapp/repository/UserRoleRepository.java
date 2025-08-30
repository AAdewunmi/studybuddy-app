package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.model.UserRole;
import com.springapplication.studybuddyapp.model.UserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link UserRole} join entities.
 */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByUser_IdAndRole_Name(Long userId, String roleName);

    List<UserRole> findAllByUser_Id(Long userId);

    @Query("select ur from UserRole ur join fetch ur.role where ur.user.id = :userId")
    List<UserRole> findAllWithRoleByUserId(@Param("userId") Long userId);

    List<UserRole> findByUser_Id(Long id);
}


