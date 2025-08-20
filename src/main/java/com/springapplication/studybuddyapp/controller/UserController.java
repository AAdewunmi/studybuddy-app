package com.springapplication.studybuddyapp.controller;

import com.springapplication.studybuddyapp.api.dto.*;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Minimal Users API for StudyBuddy.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        User u = userService.createUser(req.getName(), req.getEmail(), req.getPassword());
        return toDto(u, userService.roleNames(u));
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        User u = userService.getUser(id);
        return toDto(u, userService.roleNames(u));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.listUsers().stream()
                .map(u -> toDto(u, userService.roleNames(u)))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        User u = userService.updateProfile(id, req.getName(), req.getEmail());
        return toDto(u, userService.roleNames(u));
    }

    @PostMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(id, req.getCurrentPassword(), req.getNewPassword());
    }

    @PostMapping("/{id}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest req) {
        userService.addRole(id, req.getRoleName());
    }

    @DeleteMapping("/{id}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest req) {
        userService.removeRole(id, req.getRoleName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { userService.deleteUser(id); }

    private UserResponse toDto(User u, Set<String> roles) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), roles, u.getCreatedAt());
    }
}

