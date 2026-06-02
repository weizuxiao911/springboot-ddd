package top.archaiharness.framework.domain.tenant.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantMemberRemovedEventTest {

    @Test
    void shouldCreateTenantMemberRemovedEvent() {
        String tenantId = "123";
        String userId = "456";

        TenantMemberRemovedEvent event = new TenantMemberRemovedEvent(tenantId, userId);

        assertThat(event.getTenantId()).isEqualTo(tenantId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    void shouldHaveUniqueEventId() {
        TenantMemberRemovedEvent event1 = new TenantMemberRemovedEvent("1", "10");
        TenantMemberRemovedEvent event2 = new TenantMemberRemovedEvent("2", "20");

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldHaveEventTimestamp() {
        TenantMemberRemovedEvent event = new TenantMemberRemovedEvent("123", "456");

        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldHaveEventType() {
        TenantMemberRemovedEvent event = new TenantMemberRemovedEvent("123", "456");

        assertThat(event.getClass().getSimpleName()).isEqualTo("TenantMemberRemovedEvent");
    }
}