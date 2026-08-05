package io.github.weizuxiao911.springboot.ddd.common.pbac.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionCodeTest {

    @Test
    void shouldCreatePermissionCode() {
        PermissionCode code = new PermissionCode("USER:READ");

        assertThat(code.getCode()).isEqualTo("USER:READ");
    }

    @Test
    void shouldTrimWhitespace() {
        PermissionCode code = new PermissionCode("  USER:READ  ");

        assertThat(code.getCode()).isEqualTo("USER:READ");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsNull() {
        assertThatThrownBy(() -> new PermissionCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission code cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsEmpty() {
        assertThatThrownBy(() -> new PermissionCode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission code cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsBlank() {
        assertThatThrownBy(() -> new PermissionCode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission code cannot be null or empty");
    }

    @Test
    void shouldCreatePermissionCodeUsingOf() {
        PermissionCode code = PermissionCode.of("USER:WRITE");

        assertThat(code.getCode()).isEqualTo("USER:WRITE");
    }

    @Test
    void shouldBeEqualWhenCodesMatch() {
        PermissionCode code1 = PermissionCode.of("USER:READ");
        PermissionCode code2 = PermissionCode.of("USER:READ");

        assertThat(code1).isEqualTo(code2);
    }

    @Test
    void shouldNotBeEqualWhenCodesDiffer() {
        PermissionCode code1 = PermissionCode.of("USER:READ");
        PermissionCode code2 = PermissionCode.of("USER:WRITE");

        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void shouldHaveSameHashCodeWhenCodesMatch() {
        PermissionCode code1 = PermissionCode.of("USER:READ");
        PermissionCode code2 = PermissionCode.of("USER:READ");

        assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
    }

    @Test
    void shouldHaveDifferentHashCodeWhenCodesDiffer() {
        PermissionCode code1 = PermissionCode.of("USER:READ");
        PermissionCode code2 = PermissionCode.of("USER:WRITE");

        assertThat(code1.hashCode()).isNotEqualTo(code2.hashCode());
    }

    @Test
    void shouldReturnCodeInToString() {
        PermissionCode code = PermissionCode.of("USER:DELETE");

        assertThat(code.toString()).isEqualTo("USER:DELETE");
    }
}