package io.github.weizuxiao911.springboot.ddd.common.pbac.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessDeniedExceptionTest {

    @Test
    void shouldCreateExceptionWithPolicyCode() {
        String policyCode = "PERMISSION:DENIED";
        AccessDeniedException exception = new AccessDeniedException(policyCode);

        assertThat(exception.getPolicyCode()).isEqualTo(policyCode);
        assertThat(exception.getMessage()).contains(policyCode);
    }

    @Test
    void shouldCreateExceptionWithPolicyCodeAndMessage() {
        String policyCode = "PERMISSION:DENIED";
        String message = "Custom error message";
        AccessDeniedException exception = new AccessDeniedException(policyCode, message);

        assertThat(exception.getPolicyCode()).isEqualTo(policyCode);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldCreateExceptionWithPolicyCodeAndCause() {
        String policyCode = "PERMISSION:DENIED";
        Throwable cause = new RuntimeException("Root cause");
        AccessDeniedException exception = new AccessDeniedException(policyCode, cause);

        assertThat(exception.getPolicyCode()).isEqualTo(policyCode);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains(policyCode);
    }

    @Test
    void shouldBeRuntimeException() {
        AccessDeniedException exception = new AccessDeniedException("POLICY:CODE");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldPreserveMessageFormatWhenCreatedWithPolicyCodeOnly() {
        String policyCode = "USER:DELETE";
        AccessDeniedException exception = new AccessDeniedException(policyCode);

        assertThat(exception.getMessage())
                .isEqualTo("Access denied by policy: " + policyCode);
    }

    @Test
    void shouldPreserveMessageFormatWhenCreatedWithCause() {
        String policyCode = "USER:DELETE";
        Throwable cause = new RuntimeException("Root cause");
        AccessDeniedException exception = new AccessDeniedException(policyCode, cause);

        assertThat(exception.getMessage())
                .isEqualTo("Access denied by policy: " + policyCode);
    }
}