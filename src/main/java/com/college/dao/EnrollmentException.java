package com.college.dao;

/**
 * Thrown when student enrollment fails (e.g. duplicate email, duplicate
 * enrollment number, missing role). Carries a user-friendly message built
 * from the underlying database error.
 */
public class EnrollmentException extends RuntimeException {
    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
