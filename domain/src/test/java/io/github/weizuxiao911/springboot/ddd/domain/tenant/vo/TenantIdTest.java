package io.github.weizuxiao911.springboot.ddd.domain.tenant.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantIdTest {

    @Test
    void shouldCreateTenantId() {
        String value = "123";
        TenantId tenantId = new TenantId(value);

        assertThat(tenantId.value()).isEqualTo(value);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new TenantId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TenantId cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenValueIsBlank() {
        assertThatThrownBy(() -> new TenantId(""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TenantId cannot be empty");

        assertThatThrownBy(() -> new TenantId("   "))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TenantId cannot be empty");
    }

    @Test
    void shouldCreateTenantIdUsingOf() {
        String value = "456";
        TenantId tenantId = TenantId.of(value);

        assertThat(tenantId.value()).isEqualTo(value);
    }

    @Test
    void shouldGenerateTenantId() {
        TenantId tenantId = TenantId.generate();

        assertThat(tenantId.value()).isNotNull();
        assertThat(tenantId.value()).isNotBlank();
    }

    @Test
    void shouldReturnValueInToString() {
        String value = "789";
        TenantId tenantId = new TenantId(value);

        assertThat(tenantId.toString()).isEqualTo(value);
    }

    @Test
    void shouldBeEqualWhenValuesMatch() {
        String value = "123";
        TenantId tenantId1 = new TenantId(value);
        TenantId tenantId2 = new TenantId(value);

        assertThat(tenantId1).isEqualTo(tenantId2);
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        TenantId tenantId1 = new TenantId("123");
        TenantId tenantId2 = new TenantId("456");

        assertThat(tenantId1).isNotEqualTo(tenantId2);
    }

    @Test
    void shouldHaveSameHashCodeWhenValuesMatch() {
        String value = "123";
        TenantId tenantId1 = new TenantId(value);
        TenantId tenantId2 = new TenantId(value);

        assertThat(tenantId1.hashCode()).isEqualTo(tenantId2.hashCode());
    }
}