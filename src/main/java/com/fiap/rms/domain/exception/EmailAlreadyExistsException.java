package com.fiap.rms.domain.exception;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException(String email) {
        super("E-mail already registered: " + email);
    }
}
