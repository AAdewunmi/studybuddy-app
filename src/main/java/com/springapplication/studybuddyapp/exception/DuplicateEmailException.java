package com.springapplication.studybuddyapp.exception;
/**
 * Exception thrown when attempting to create a user with an email
 * that already exists.
 */

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {}
    public DuplicateEmailException(String message) { super(message); }
}

