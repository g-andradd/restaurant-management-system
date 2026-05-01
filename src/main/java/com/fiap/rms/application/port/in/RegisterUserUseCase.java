package com.fiap.rms.application.port.in;

import com.fiap.rms.application.usecase.RegisterUserCommand;
import com.fiap.rms.domain.model.User;

public interface RegisterUserUseCase {

    User register(RegisterUserCommand command);
}
