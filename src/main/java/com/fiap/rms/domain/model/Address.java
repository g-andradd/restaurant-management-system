package com.fiap.rms.domain.model;

import com.fiap.rms.domain.exception.InvalidAddressException;

public record Address(String street, String number, String city, String zipCode) {

    public Address {
        requireNonBlank(street, "street");
        requireNonBlank(number, "number");
        requireNonBlank(city, "city");
        requireNonBlank(zipCode, "zipCode");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidAddressException(field + " must not be blank");
        }
    }
}
