package io.github.weizuxiao911.springboot.ddd.domain.user.entity;

import io.github.weizuxiao911.springboot.ddd.common.exception.DomainException;
import io.github.weizuxiao911.springboot.ddd.domain.user.event.UserCreatedEvent;
import io.github.weizuxiao911.springboot.ddd.domain.user.event.UserUpdatedEvent;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateUserWithActiveStatus() {
        User user = User.create("zhangsan", "zhangsan@example.com");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("zhangsan");
        assertThat(user.getEmail()).isEqualTo("zhangsan@example.com");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullUsername() {
        assertThatThrownBy(() -> User.create(null, "email@test.com"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Username cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithBlankUsername() {
        assertThatThrownBy(() -> User.create("   ", "email@test.com"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Username cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullEmail() {
        assertThatThrownBy(() -> User.create("zhangsan", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Email cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithBlankEmail() {
        assertThatThrownBy(() -> User.create("zhangsan", "   "))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Email cannot be empty");
    }

    @Test
    void shouldRegisterUserCreatedEventWhenCreating() {
        User user = User.create("zhangsan", "zhangsan@example.com");

        List<?> events = user.getDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(UserCreatedEvent.class);

        UserCreatedEvent event = (UserCreatedEvent) events.get(0);
        assertThat(event.getUserId()).isEqualTo(user.getId().value());
        assertThat(event.getUsername()).isEqualTo("zhangsan");
    }

    @Test
    void shouldUpdateEmailWhenEmailIsValid() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.clearDomainEvents();

        user.updateEmail("newemail@example.com");

        assertThat(user.getEmail()).isEqualTo("newemail@example.com");
        assertThat(user.getDomainEvents()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingEmailWithBlank() {
        User user = User.create("zhangsan", "zhangsan@example.com");

        assertThatThrownBy(() -> user.updateEmail(""))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATE");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingEmailWithNull() {
        User user = User.create("zhangsan", "zhangsan@example.com");

        assertThatThrownBy(() -> user.updateEmail(null))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATE");
    }

    @Test
    void shouldUpdatePhone() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.clearDomainEvents();

        user.updatePhone("13800138000");

        assertThat(user.getPhone()).isEqualTo("13800138000");
        assertThat(user.getDomainEvents()).hasSize(1);
    }

    @Test
    void shouldDeactivateUser() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.clearDomainEvents();

        user.deactivate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.DEACTIVATED);
        assertThat(user.getDomainEvents()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingAlreadyDeactivatedUser() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.deactivate();
        user.clearDomainEvents();

        assertThatThrownBy(user::deactivate)
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATE");
    }

    @Test
    void shouldActivateUser() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.deactivate();
        user.clearDomainEvents();

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getDomainEvents()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenActivatingAlreadyActiveUser() {
        User user = User.create("zhangsan", "zhangsan@example.com");

        assertThatThrownBy(user::activate)
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATE");
    }

    @Test
    void shouldReturnTrueWhenUserIsActive() {
        User user = User.create("zhangsan", "zhangsan@example.com");

        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotActive() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.deactivate();

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void shouldRegisterUserUpdatedEventWhenUpdatingField() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        user.clearDomainEvents();

        user.updateEmail("new@example.com");

        UserUpdatedEvent event = (UserUpdatedEvent) user.getDomainEvents().get(0);
        assertThat(event.getUserId()).isEqualTo(user.getId().value());
        assertThat(event.getField()).isEqualTo("email");
    }

    @Test
    void shouldClearDomainEvents() {
        User user = User.create("zhangsan", "zhangsan@example.com");
        assertThat(user.getDomainEvents()).isNotEmpty();

        user.clearDomainEvents();

        assertThat(user.getDomainEvents()).isEmpty();
    }
}
