package com.fiap.rms.application.port.in;

import com.fiap.rms.application.usecase.AuthenticationResult;

public interface AuthenticateUserUseCase {

    AuthenticationResult authenticate(String login, String rawPassword);
}
