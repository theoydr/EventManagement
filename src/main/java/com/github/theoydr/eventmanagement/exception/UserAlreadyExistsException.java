package com.github.theoydr.eventmanagement.exception;

import java.util.Map;

public class UserAlreadyExistsException extends RuntimeException implements StructuredError {

    private final String email;

    public UserAlreadyExistsException(String email) {
        // The message is still generated for logging and fallback purposes.
        super(String.format("User with email '%s' already exists.", email));
        this.email = email;
    }


    @Override
    public Map<String, Object> getArguments() {
        return Map.of("email", email);
    }

    public String getEmail() {
        return email;
    }
}