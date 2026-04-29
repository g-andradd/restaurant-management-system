package com.fiap.rms.application.port.in;

import com.fiap.rms.application.usecase.UpdateUserCommand;
import com.fiap.rms.domain.model.User;

import java.util.UUID;

public interface UpdateUserUseCase {

    User update(UUID id, UpdateUserCommand command);
}
