package com.fiap.rms.application.usecase;

import com.fiap.rms.domain.model.Address;

public record UpdateUserCommand(
        String name,
        String email,
        String login,
        Address address
) {}
