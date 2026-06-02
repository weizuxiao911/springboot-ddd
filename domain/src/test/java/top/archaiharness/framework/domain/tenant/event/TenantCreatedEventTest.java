package top.archaiharness.framework.domain.tenant.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantCreatedEventTest {

    @Test
    void shouldCreateTenantCreatedEvent() {
        String tenantId = "123";
        String tenantName = "Test Tenant";
        String ownerUserId = "456";

        TenantCreatedEvent event = new TenantCreatedEvent(tenantId, tenantName, ownerUserId);

        assertThat(event.getTenantId()).isEqualTo(tenantId);
        assertThat(event.getTenantName()).isEqualTo(tenantName);
        assertThat(event.getOwnerUserId()).isEqualTo(ownerUserId);
        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    void shouldHaveUniqueEventId() {
        TenantCreatedEvent event1 = new TenantCreatedEvent("1", "Tenant1", "10");
        TenantCreatedEvent event2 = new TenantCreatedEvent("2", "Tenant2", "20");

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    void shouldHaveEventTimestamp() {
        TenantCreatedEvent event = new TenantCreatedEvent("123", "Test", "456");

        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldHaveEventType() {
        TenantCreatedEvent event = new TenantCreatedEvent("123", "Test", "456");

        assertThat(event.getClass().getSimpleName()).isEqualTo("TenantCreatedEvent");
    }
}