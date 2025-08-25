package com.springapplication.studybuddyapp.service;

import com.springapplication.studybuddyapp.exception.DuplicateEmailException;
import com.springapplication.studybuddyapp.controller.SignupForm;

public interface UserServiceInterface {
    boolean existsByEmail(String email);
}

