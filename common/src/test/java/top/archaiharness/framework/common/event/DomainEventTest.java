package top.archaiharness.framework.common.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void shouldAutoGenerateEventIdAndOccurredAt() {
        TestEvent event = new TestEvent();

        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(event.getEventType()).isEqualTo("TestEvent");
    }

    static class TestEvent extends DomainEvent {
    }
}
