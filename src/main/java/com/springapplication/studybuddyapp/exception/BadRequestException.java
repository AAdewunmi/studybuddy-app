// path: src/main/java/com/springapplication/studybuddyapp/exception/BadRequestException.java
package com.springapplication.studybuddyapp.exception;

/** 400 Bad Request for validation or mismatch errors. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}

