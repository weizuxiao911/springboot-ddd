package top.archaiharness.framework.domain.tenant.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantRoleTest {

    @Test
    void shouldHaveThreeRoles() {
        assertThat(TenantRole.values()).hasSize(3);
    }

    @Test
    void shouldHaveOwnerRole() {
        assertThat(TenantRole.OWNER).isNotNull();
        assertThat(TenantRole.OWNER.name()).isEqualTo("OWNER");
    }

    @Test
    void shouldHaveAdminRole() {
        assertThat(TenantRole.ADMIN).isNotNull();
        assertThat(TenantRole.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHaveMemberRole() {
        assertThat(TenantRole.MEMBER).isNotNull();
        assertThat(TenantRole.MEMBER.name()).isEqualTo("MEMBER");
    }

    @Test
    void shouldBeAbleToFindByValueOf() {
        assertThat(TenantRole.valueOf("OWNER")).isEqualTo(TenantRole.OWNER);
        assertThat(TenantRole.valueOf("ADMIN")).isEqualTo(TenantRole.ADMIN);
        assertThat(TenantRole.valueOf("MEMBER")).isEqualTo(TenantRole.MEMBER);
    }

    @Test
    void shouldHaveSameInstanceWhenValueOfSameName() {
        assertThat(TenantRole.valueOf("OWNER")).isSameAs(TenantRole.OWNER);
        assertThat(TenantRole.valueOf("ADMIN")).isSameAs(TenantRole.ADMIN);
        assertThat(TenantRole.valueOf("MEMBER")).isSameAs(TenantRole.MEMBER);
    }
}