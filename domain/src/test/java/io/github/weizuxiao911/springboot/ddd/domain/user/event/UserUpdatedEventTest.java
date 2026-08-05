package io.github.weizuxiao911.springboot.ddd.domain.user.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpdatedEventTest {

    @Test
    void shouldCreateUserUpdatedEvent() {
        String userId = "123";
        String field = "email";

        UserUpdatedEvent event = new UserUpdatedEvent(userId, field);

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getField()).isEqualTo(field);
        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    void shouldHaveUniqueEventId() {
        UserUpdatedEvent event1 = new UserUpdatedEvent("1", "email");
        UserUpdatedEvent event2 = new UserUpdatedEvent("2", "phone");

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldHaveEventTimestamp() {
        UserUpdatedEvent event = new UserUpdatedEvent("123", "email");

        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldHaveEventType() {
        UserUpdatedEvent event = new UserUpdatedEvent("123", "email");

        assertThat(event.getClass().getSimpleName()).isEqualTo("UserUpdatedEvent");
    }

    @Test
    void shouldSupportDifferentFields() {
        UserUpdatedEvent emailEvent = new UserUpdatedEvent("123", "email");
        UserUpdatedEvent phoneEvent = new UserUpdatedEvent("123", "phone");
        UserUpdatedEvent statusEvent = new UserUpdatedEvent("123", "status");

        assertThat(emailEvent.getField()).isEqualTo("email");
        assertThat(phoneEvent.getField()).isEqualTo("phone");
        assertThat(statusEvent.getField()).isEqualTo("status");
    }
}