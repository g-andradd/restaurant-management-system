package com.fiap.rms.application.port.in;

import com.fiap.rms.domain.model.User;

import java.util.UUID;

public interface FindUserByIdUseCase {

    User findById(UUID id);
}
