// path: src/main/java/com/springapplication/studybuddyapp/exception/ConflictException.java
package com.springapplication.studybuddyapp.exception;

/** 409 Conflict (e.g., duplicate email). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}

