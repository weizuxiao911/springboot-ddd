package io.github.weizuxiao911.springboot.ddd.domain.common;

import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        DomainException exception = DomainException.of("Test error");

        assertThat(exception.getMessage()).isEqualTo("Test error");
        assertThat(exception.getCode()).isEqualTo("DOMAIN_ERROR");
    }

    @Test
    void shouldCreateExceptionWithCodeAndMessage() {
        DomainException exception = DomainException.of("CUSTOM_CODE", "Custom error");

        assertThat(exception.getMessage()).isEqualTo("Custom error");
        assertThat(exception.getCode()).isEqualTo("CUSTOM_CODE");
    }

    @Test
    void shouldCreateNotFoundException() {
        DomainException exception = DomainException.notFound("User", "123");

        assertThat(exception.getMessage()).isEqualTo("User not found: 123");
        assertThat(exception.getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void shouldCreateAlreadyExistsException() {
        DomainException exception = DomainException.alreadyExists("User", "test@example.com");

        assertThat(exception.getMessage()).isEqualTo("User already exists: test@example.com");
        assertThat(exception.getCode()).isEqualTo("ALREADY_EXISTS");
    }

    @Test
    void shouldCreateInvalidStateException() {
        DomainException exception = DomainException.invalidState("Invalid operation");

        assertThat(exception.getMessage()).isEqualTo("Invalid operation");
        assertThat(exception.getCode()).isEqualTo("INVALID_STATE");
    }

    @Test
    void shouldBeThrowable() {
        assertThatThrownBy(() -> { throw DomainException.of("Test"); })
                .isInstanceOf(DomainException.class);
    }
}
