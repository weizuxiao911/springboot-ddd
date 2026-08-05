package io.github.weizuxiao911.springboot.ddd.common.pbac.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationResultTest {

    @Test
    void shouldCreateAllowedResult() {
        EvaluationResult result = EvaluationResult.allow("PERMISSION:READ");

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getPermissionCode()).isEqualTo("PERMISSION:READ");
    }

    @Test
    void shouldCreateDeniedResult() {
        EvaluationResult result = EvaluationResult.deny("PERMISSION:WRITE");

        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getPermissionCode()).isEqualTo("PERMISSION:WRITE");
    }

    @Test
    void shouldHaveCorrectStateForAllow() {
        EvaluationResult result = EvaluationResult.allow("USER:CREATE");

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getPermissionCode()).isEqualTo("USER:CREATE");
    }

    @Test
    void shouldHaveCorrectStateForDeny() {
        EvaluationResult result = EvaluationResult.deny("USER:DELETE");

        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getPermissionCode()).isEqualTo("USER:DELETE");
    }
}