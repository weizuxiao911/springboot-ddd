package top.archaiharness.framework.domain.user.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

    @Test
    void shouldCreateUserId() {
        String value = "123";
        UserId userId = new UserId(value);

        assertThat(userId.value()).isEqualTo(value);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UserId cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenValueIsBlank() {
        assertThatThrownBy(() -> new UserId(""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UserId cannot be empty");

        assertThatThrownBy(() -> new UserId("  "))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UserId cannot be empty");
    }

    @Test
    void shouldCreateUserIdUsingOf() {
        String value = "456";
        UserId userId = UserId.of(value);

        assertThat(userId.value()).isEqualTo(value);
    }

    @Test
    void shouldGenerateUserId() {
        UserId userId = UserId.generate();

        assertThat(userId.value()).isNotNull();
        assertThat(userId.value()).isNotBlank();
    }

    @Test
    void shouldReturnValueInToString() {
        String value = "789";
        UserId userId = new UserId(value);

        assertThat(userId.toString()).isEqualTo(value);
    }

    @Test
    void shouldBeEqualWhenValuesMatch() {
        String value = "123";
        UserId userId1 = new UserId(value);
        UserId userId2 = new UserId(value);

        assertThat(userId1).isEqualTo(userId2);
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        UserId userId1 = new UserId("123");
        UserId userId2 = new UserId("456");

        assertThat(userId1).isNotEqualTo(userId2);
    }

    @Test
    void shouldHaveSameHashCodeWhenValuesMatch() {
        String value = "123";
        UserId userId1 = new UserId(value);
        UserId userId2 = new UserId(value);

        assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
    }
}