package top.archaiharness.framework.common.base;

import top.archaiharness.framework.common.event.DomainEvent;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class AggregateRootTest {

    @Test
    void shouldRegisterDomainEvents() {
        TestAggregate aggregate = new TestAggregate();
        TestEvent event = new TestEvent();

        aggregate.registerEvent(event);

        assertThat(aggregate.getDomainEvents()).hasSize(1);
        assertThat(aggregate.getDomainEvents().get(0)).isSameAs(event);
    }

    @Test
    void shouldReturnUnmodifiableEventList() {
        TestAggregate aggregate = new TestAggregate();

        assertThat(aggregate.getDomainEvents()).isUnmodifiable();
    }

    @Test
    void shouldClearDomainEvents() {
        TestAggregate aggregate = new TestAggregate();
        aggregate.registerEvent(new TestEvent());
        aggregate.registerEvent(new TestEvent());

        aggregate.clearDomainEvents();

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }

    static class TestAggregate extends AggregateRoot {
    }

    static class TestEvent extends DomainEvent {
    }
}
