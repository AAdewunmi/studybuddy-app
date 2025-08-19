package com.springapplication.studybuddyapp.services;

import com.springapplication.studybuddyapp.dto.RegisterRequest;
import com.springapplication.studybuddyapp.model.User;
import com.springapplication.studybuddyapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // injected BCrypt

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ... existing code ...
    public User register(RegisterRequest request) {
        User user = createUser(request);
        // set roles, etc.
        return userRepository.save(user);
    }
    // ... existing code ...

    private User createUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getDisplayName());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(encodedPassword);

        return user;
    }
    // ... existing code ...

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Treat 'username' as email for authentication
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + username));

        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}

