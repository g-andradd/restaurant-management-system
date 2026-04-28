package com.fiap.rms.domain.model;

import com.fiap.rms.domain.exception.InvalidAddressException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Test
    void create_withValidData_succeeds() {
        Address address = new Address("Rua das Flores", "123", "São Paulo", "01310-100");

        assertThat(address.street()).isEqualTo("Rua das Flores");
        assertThat(address.number()).isEqualTo("123");
        assertThat(address.city()).isEqualTo("São Paulo");
        assertThat(address.zipCode()).isEqualTo("01310-100");
    }

    @Test
    void create_withBlankStreet_throwsInvalidAddressException() {
        assertThatThrownBy(() -> new Address("   ", "123", "São Paulo", "01310-100"))
                .isInstanceOf(InvalidAddressException.class);
    }
}
