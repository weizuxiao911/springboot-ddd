package io.github.weizuxiao911.springboot.ddd.domain.user.vo;

import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserStatusTest {

    @Test
    void shouldReturnActiveStatus() {
        UserStatus status = UserStatus.fromValue("active");

        assertThat(status).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldReturnDeactivatedStatus() {
        UserStatus status = UserStatus.fromValue("deactivated");

        assertThat(status).isEqualTo(UserStatus.DEACTIVATED);
    }

    @Test
    void shouldReturnLockedStatus() {
        UserStatus status = UserStatus.fromValue("locked");

        assertThat(status).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void shouldThrowDomainExceptionForUnknownValue() {
        assertThatThrownBy(() -> UserStatus.fromValue("unknown"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Unknown UserStatus: unknown");
    }

    @Test
    void shouldHaveCorrectValueForActive() {
        assertThat(UserStatus.ACTIVE.getValue()).isEqualTo("active");
    }

    @Test
    void shouldHaveCorrectValueForDeactivated() {
        assertThat(UserStatus.DEACTIVATED.getValue()).isEqualTo("deactivated");
    }

    @Test
    void shouldHaveCorrectValueForLocked() {
        assertThat(UserStatus.LOCKED.getValue()).isEqualTo("locked");
    }
}
