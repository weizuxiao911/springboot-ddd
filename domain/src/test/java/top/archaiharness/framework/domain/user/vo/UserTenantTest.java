package top.archaiharness.framework.domain.user.vo;

import top.archaiharness.framework.domain.tenant.vo.TenantId;
import top.archaiharness.framework.domain.tenant.vo.TenantRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTenantTest {

    @Test
    void shouldCreateUserTenant() {
        TenantId tenantId = TenantId.generate();
        TenantRole role = TenantRole.MEMBER;

        UserTenant userTenant = new UserTenant(tenantId, role);

        assertThat(userTenant.tenantId()).isEqualTo(tenantId);
        assertThat(userTenant.role()).isEqualTo(role);
    }

    @Test
    void shouldThrowExceptionWhenTenantIdIsNull() {
        assertThatThrownBy(() -> new UserTenant(null, TenantRole.MEMBER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TenantId cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {
        TenantId tenantId = TenantId.generate();

        assertThatThrownBy(() -> new UserTenant(tenantId, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role cannot be null");
    }

    @Test
    void shouldCreateUserTenantUsingOf() {
        String tenantIdStr = "123";
        String roleStr = "ADMIN";

        UserTenant userTenant = UserTenant.of(tenantIdStr, roleStr);

        assertThat(userTenant.tenantId()).isEqualTo(TenantId.of(tenantIdStr));
        assertThat(userTenant.role()).isEqualTo(TenantRole.ADMIN);
    }

    @Test
    void shouldThrowExceptionWhenOfWithInvalidRole() {
        String tenantIdStr = "123";
        String roleStr = "INVALID_ROLE";

        assertThatThrownBy(() -> UserTenant.of(tenantIdStr, roleStr))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnTrueWhenIsOwner() {
        UserTenant owner = new UserTenant(TenantId.generate(), TenantRole.OWNER);

        assertThat(owner.isOwner()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsNotOwner() {
        UserTenant member = new UserTenant(TenantId.generate(), TenantRole.MEMBER);

        assertThat(member.isOwner()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenHasAdminPrivileges() {
        UserTenant owner = new UserTenant(TenantId.generate(), TenantRole.OWNER);
        UserTenant admin = new UserTenant(TenantId.generate(), TenantRole.ADMIN);

        assertThat(owner.hasAdminPrivileges()).isTrue();
        assertThat(admin.hasAdminPrivileges()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenHasNoAdminPrivileges() {
        UserTenant member = new UserTenant(TenantId.generate(), TenantRole.MEMBER);

        assertThat(member.hasAdminPrivileges()).isFalse();
    }

    @Test
    void shouldBeEqualWhenAllFieldsMatch() {
        TenantId tenantId = TenantId.generate();
        TenantRole role = TenantRole.MEMBER;

        UserTenant userTenant1 = new UserTenant(tenantId, role);
        UserTenant userTenant2 = new UserTenant(tenantId, role);

        assertThat(userTenant1).isEqualTo(userTenant2);
    }

    @Test
    void shouldNotBeEqualWhenFieldsDiffer() {
        UserTenant userTenant1 = new UserTenant(TenantId.generate(), TenantRole.MEMBER);
        UserTenant userTenant2 = new UserTenant(TenantId.generate(), TenantRole.MEMBER);

        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    @Test
    void shouldHaveSameHashCodeWhenAllFieldsMatch() {
        TenantId tenantId = TenantId.generate();
        TenantRole role = TenantRole.MEMBER;

        UserTenant userTenant1 = new UserTenant(tenantId, role);
        UserTenant userTenant2 = new UserTenant(tenantId, role);

        assertThat(userTenant1.hashCode()).isEqualTo(userTenant2.hashCode());
    }

    @Test
    void shouldHaveDifferentHashCodeWhenFieldsDiffer() {
        UserTenant userTenant1 = new UserTenant(TenantId.generate(), TenantRole.MEMBER);
        UserTenant userTenant2 = new UserTenant(TenantId.generate(), TenantRole.MEMBER);

        assertThat(userTenant1.hashCode()).isNotEqualTo(userTenant2.hashCode());
    }
}