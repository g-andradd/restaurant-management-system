package com.fiap.rms.application.port.out;

import com.fiap.rms.application.usecase.AuthenticationResult;

public interface AuthenticationStrategyPort {

    AuthenticationResult authenticate(String login, String rawPassword);
}
