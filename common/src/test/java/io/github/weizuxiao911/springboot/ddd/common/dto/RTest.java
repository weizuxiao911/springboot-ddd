package io.github.weizuxiao911.springboot.ddd.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("R Tests")
class RTest {

    @Nested
    @DisplayName("ok method")
    class OkMethodTests {

        @Test
        @DisplayName("should return R with success code and data")
        void shouldReturnRWithSuccessCodeAndDataWhenDataProvided() {
            String data = "test data";
            R<String> result = R.ok(data);

            assertThat(result.getCode()).isEqualTo("0");
            assertThat(result.getData()).isEqualTo(data);
            assertThat(result.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("should return R with success code when null data")
        void shouldReturnRWithSuccessCodeWhenNullData() {
            R<String> result = R.ok(null);

            assertThat(result.getCode()).isEqualTo("0");
            assertThat(result.getData()).isNull();
            assertThat(result.getSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("fail method")
    class FailMethodTests {

        @Test
        @DisplayName("should return R with default error code and message")
        void shouldReturnRWithDefaultErrorCodeAndMessageWhenOnlyMessageProvided() {
            String message = "error occurred";
            R<String> result = R.fail(message);

            assertThat(result.getCode().equals("-1"));
            assertThat(result.getMessage()).isEqualTo(message);
            assertThat(result.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("should return R with custom error code and message")
        void shouldReturnRWithCustomErrorCodeAndMessageWhenCodeAndMessageProvided() {
            String code = "400";
            String message = "bad request";
            R<String> result = R.fail(code, message);

            assertThat(result.getCode()).isEqualTo(code);
            assertThat(result.getMessage()).isEqualTo(message);
            assertThat(result.getSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("getSuccess method")
    class GetSuccessMethodTests {

        @Test
        @DisplayName("should return true when code is '0'")
        void shouldReturnTrueWhenCodeIsZero() {
            R<String> result = R.<String>builder().code("0").build();

            assertThat(result.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("should return false when code is not '0'")
        void shouldReturnFalseWhenCodeIsNotZero() {
            R<String> result = R.<String>builder().code("-1").build();

            assertThat(result.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("should return false when code is null")
        void shouldReturnFalseWhenCodeIsNull() {
            R<String> result = R.<String>builder().build();

            assertThat(result.getSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder pattern")
    class BuilderPatternTests {

        @Test
        @DisplayName("should build complete R object")
        void shouldBuildCompleteRObjectWhenAllFieldsProvided() {
            R<String> result = R.<String>builder()
                    .code("200")
                    .message("success")
                    .data("test")
                    .build();

            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getMessage()).isEqualTo("success");
            assertThat(result.getData()).isEqualTo("test");
        }
    }
}