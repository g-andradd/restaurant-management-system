package com.fiap.rms.application.port.in;

import java.util.UUID;

public interface ChangePasswordUseCase {

    void changePassword(UUID id, String newPlainPassword);
}
