// path: src/main/java/com/springapplication/studybuddyapp/security/CustomUserDetailsService.java
package com.springapplication.studybuddyapp.service;

import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.repository.UserRepository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Loads users from the database for Spring Security authentication.
 * We use email as the username field.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    /** Loads a user by email and maps domain roles to Spring authorities. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = users.findByEmailIgnoreCaseWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Collection<GrantedAuthority> authorities = u.getUserRoles().stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(false)
                .build();
    }
}

