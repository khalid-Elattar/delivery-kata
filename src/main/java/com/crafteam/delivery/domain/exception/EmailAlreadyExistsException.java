package com.crafteam.delivery.domain.exception;

/**
 * Exception thrown when trying to register a user with an email that already exists.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
