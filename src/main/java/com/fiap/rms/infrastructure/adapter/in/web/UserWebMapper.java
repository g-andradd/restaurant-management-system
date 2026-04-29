package com.fiap.rms.infrastructure.adapter.in.web;

import com.fiap.rms.application.usecase.RegisterUserCommand;
import com.fiap.rms.domain.model.Address;
import com.fiap.rms.domain.model.User;
import com.fiap.rms.infrastructure.adapter.in.web.dto.AddressRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.fiap.rms.infrastructure.adapter.in.web.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public RegisterUserCommand toCommand(RegisterUserRequest request) {
        AddressRequest a = request.address();
        return new RegisterUserCommand(
                request.name(),
                request.email(),
                request.login(),
                request.password(),
                request.role(),
                new Address(a.street(), a.number(), a.city(), a.zipCode())
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getRole(),
                user.getAddress(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
