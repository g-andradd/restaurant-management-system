package com.fiap.rms.application.usecase;

import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.Role;

public record RegisterUserCommand(
        String name,
        String email,
        String login,
        String password,
        Role role,
        Address address
) {}
