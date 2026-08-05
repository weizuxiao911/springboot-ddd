package io.github.weizuxiao911.springboot.ddd.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        DomainException ex = DomainException.of("Error message");

        assertThat(ex.getCode()).isEqualTo("DOMAIN_ERROR");
        assertThat(ex.getMessage()).isEqualTo("Error message");
    }

    @Test
    void shouldCreateExceptionWithCodeAndMessage() {
        DomainException ex = DomainException.of("CUSTOM_CODE", "Custom error");

        assertThat(ex.getCode()).isEqualTo("CUSTOM_CODE");
        assertThat(ex.getMessage()).isEqualTo("Custom error");
    }

    @Test
    void shouldCreateNotFoundException() {
        DomainException ex = DomainException.notFound("User", "123");

        assertThat(ex.getCode()).isEqualTo("NOT_FOUND");
        assertThat(ex.getMessage()).isEqualTo("User not found: 123");
    }

    @Test
    void shouldCreateAlreadyExistsException() {
        DomainException ex = DomainException.alreadyExists("User", "zhangsan");

        assertThat(ex.getCode()).isEqualTo("ALREADY_EXISTS");
        assertThat(ex.getMessage()).isEqualTo("User already exists: zhangsan");
    }

    @Test
    void shouldCreateInvalidStateException() {
        DomainException ex = DomainException.invalidState("Invalid state");

        assertThat(ex.getCode()).isEqualTo("INVALID_STATE");
        assertThat(ex.getMessage()).isEqualTo("Invalid state");
    }
}
