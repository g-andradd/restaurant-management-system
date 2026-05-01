package com.fiap.rms.domain.exception;

public class LoginAlreadyExistsException extends DomainException {

    public LoginAlreadyExistsException(String login) {
        super("Login already registered: " + login);
    }
}
