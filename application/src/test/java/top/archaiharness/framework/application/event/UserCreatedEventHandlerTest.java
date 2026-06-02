package top.archaiharness.framework.application.event;

import top.archaiharness.framework.domain.user.event.UserCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserCreatedEventHandlerTest {

    @Test
    void shouldHandleUserCreatedEvent() {
        UserCreatedEventHandler handler = new UserCreatedEventHandler();
        UserCreatedEvent event = new UserCreatedEvent("user-001", "zhangsan");

        assertDoesNotThrow(() -> handler.handle(event));
    }

    @Test
    void shouldHandleDuplicateEvent() {
        UserCreatedEventHandler handler = new UserCreatedEventHandler();
        UserCreatedEvent event = new UserCreatedEvent("user-001", "zhangsan");

        handler.handle(event);
        assertDoesNotThrow(() -> handler.handle(event));
    }
}