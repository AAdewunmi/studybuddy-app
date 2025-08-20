package com.springapplication.studybuddyapp.exception;

/** 404 Not Found for domain resources. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
