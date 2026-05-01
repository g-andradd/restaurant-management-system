package com.fiap.rms.application.port.in;

import com.fiap.rms.domain.model.User;

import java.util.List;

public interface SearchUsersByNameUseCase {

    List<User> searchByName(String term);
}
