package top.archaiharness.framework.domain.tenant.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantMemberAddedEventTest {

    @Test
    void shouldCreateTenantMemberAddedEvent() {
        String tenantId = "123";
        String userId = "456";
        String role = "ADMIN";

        TenantMemberAddedEvent event = new TenantMemberAddedEvent(tenantId, userId, role);

        assertThat(event.getTenantId()).isEqualTo(tenantId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getRole()).isEqualTo(role);
        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    void shouldHaveUniqueEventId() {
        TenantMemberAddedEvent event1 = new TenantMemberAddedEvent("1", "10", "ADMIN");
        TenantMemberAddedEvent event2 = new TenantMemberAddedEvent("2", "20", "MEMBER");

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldHaveEventTimestamp() {
        TenantMemberAddedEvent event = new TenantMemberAddedEvent("123", "456", "MEMBER");

        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldHaveEventType() {
        TenantMemberAddedEvent event = new TenantMemberAddedEvent("123", "456", "ADMIN");

        assertThat(event.getClass().getSimpleName()).isEqualTo("TenantMemberAddedEvent");
    }
}