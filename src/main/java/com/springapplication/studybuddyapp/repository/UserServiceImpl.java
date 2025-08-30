package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.exception.DuplicateEmailException;
import com.springapplication.studybuddyapp.model.Role;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.model.UserRole;

import java.util.List;
import java.util.stream.Collectors;

import com.springapplication.studybuddyapp.service.UserServiceInterface;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Service
public class UserServiceImpl implements UserServiceInterface, UserDetailsService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository users, RoleRepository roles, UserRoleRepository userRoles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.encoder = encoder;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    @Transactional
    public User register(String name, String email, String rawPassword) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }
        String hash = encoder.encode(rawPassword);
        User u = new User(name, email, hash);
        User saved = users.save(u);

        Role userRole = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        userRoles.save(new UserRole(saved, userRole));
        return saved;
    }

    @Override
    public User findByEmail(String email) {
        return users.findByEmailIgnoreCase(email).orElse(null);
    }

    @Override
    public boolean emailExists(String email) {
        return users.existsByEmailIgnoreCase(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException(email));
        List<GrantedAuthority> auths = userRoles.findAllWithRoleByUserId(user.getId())
                .stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName()))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), auths);
    }
}

