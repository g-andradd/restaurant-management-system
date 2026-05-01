package com.fiap.rms.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }
}
