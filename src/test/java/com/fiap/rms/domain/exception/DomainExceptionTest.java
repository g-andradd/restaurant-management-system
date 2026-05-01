package com.fiap.rms.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void messagePropagatesThroughInlineSubclass() {
        String expected = "something went wrong in the domain";

        DomainException ex = new DomainException(expected) {};

        assertThat(ex.getMessage()).isEqualTo(expected);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
