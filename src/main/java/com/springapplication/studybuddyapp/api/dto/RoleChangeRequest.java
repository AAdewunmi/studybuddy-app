// path: src/main/java/com/springapplication/studybuddyapp/api/dto/RoleChangeRequest.java
package com.springapplication.studybuddyapp.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body to add/remove a role. */
public class RoleChangeRequest {
    @NotBlank
    private String roleName;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}

