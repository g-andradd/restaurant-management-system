package com.fiap.rms.application.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {

    void delete(UUID id);
}
