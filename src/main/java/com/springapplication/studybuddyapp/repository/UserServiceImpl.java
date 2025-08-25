package com.springapplication.studybuddyapp.repository;

import com.springapplication.studybuddyapp.repository.UserRepository;
import com.springapplication.studybuddyapp.service.UserServiceInterface;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserServiceImpl implements UserServiceInterface {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return userRepository.existsByEmail(email.trim().toLowerCase(Locale.ROOT));
    }

    // keep your other concrete methods here if you want, they don't have to be on the interface
}

