package io.github.weizuxiao911.springboot.ddd.domain.tenant.entity;

import io.github.weizuxiao911.springboot.ddd.domain.tenant.event.TenantCreatedEvent;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantTest {

    @Test
    void shouldCreateTenant() {
        String name = "Test Tenant";
        UserId ownerUserId = UserId.generate();

        Tenant tenant = Tenant.create(name, ownerUserId);

        assertThat(tenant.getId()).isNotNull();
        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getDomainEvents()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        UserId ownerUserId = UserId.generate();

        assertThatThrownBy(() -> Tenant.create(null, ownerUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant name cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        UserId ownerUserId = UserId.generate();

        assertThatThrownBy(() -> Tenant.create("", ownerUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant name cannot be empty");

        assertThatThrownBy(() -> Tenant.create("   ", ownerUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant name cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenOwnerUserIdIsNull() {
        assertThatThrownBy(() -> Tenant.create("Test Tenant", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Owner user cannot be null");
    }

    @Test
    void shouldRegisterTenantCreatedEvent() {
        String name = "Test Tenant";
        UserId ownerUserId = UserId.generate();

        Tenant tenant = Tenant.create(name, ownerUserId);

        assertThat(tenant.getDomainEvents()).hasSize(1);

        TenantCreatedEvent event = (TenantCreatedEvent) tenant.getDomainEvents().get(0);
        assertThat(event.getTenantId()).isEqualTo(tenant.getId().value());
        assertThat(event.getTenantName()).isEqualTo(name);
        assertThat(event.getOwnerUserId()).isEqualTo(ownerUserId.toString());
    }

    @Test
    void shouldUpdateName() {
        Tenant tenant = Tenant.create("Old Name", UserId.generate());
        String newName = "New Name";

        tenant.updateName(newName);

        assertThat(tenant.getName()).isEqualTo(newName);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToNull() {
        Tenant tenant = Tenant.create("Test Tenant", UserId.generate());

        assertThatThrownBy(() -> tenant.updateName(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant name cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToBlank() {
        Tenant tenant = Tenant.create("Test Tenant", UserId.generate());

        assertThatThrownBy(() -> tenant.updateName(""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant name cannot be empty");
    }

    @Test
    void shouldCheckUserIsMember() {
        UserId userId = UserId.generate();
        Tenant tenant = Tenant.create("Test Tenant", UserId.generate());
        TenantMember member = TenantMember.create(tenant.getId(), userId, io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole.MEMBER);

        boolean isMember = tenant.hasMember(userId, List.of(member));

        assertThat(isMember).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotMember() {
        UserId userId = UserId.generate();
        Tenant tenant = Tenant.create("Test Tenant", UserId.generate());
        TenantMember member = TenantMember.create(tenant.getId(), UserId.generate(), io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole.MEMBER);

        boolean isMember = tenant.hasMember(userId, List.of(member));

        assertThat(isMember).isFalse();
    }

    @Test
    void shouldCheckUserIsOwner() {
        UserId ownerUserId = UserId.generate();
        Tenant tenant = Tenant.create("Test Tenant", ownerUserId);
        TenantMember ownerMember = TenantMember.create(tenant.getId(), ownerUserId, io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole.OWNER);

        boolean isOwner = tenant.isOwner(ownerUserId, List.of(ownerMember));

        assertThat(isOwner).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotOwner() {
        UserId userId = UserId.generate();
        Tenant tenant = Tenant.create("Test Tenant", userId);
        TenantMember member = TenantMember.create(tenant.getId(), userId, io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole.MEMBER);

        boolean isOwner = tenant.isOwner(userId, List.of(member));

        assertThat(isOwner).isFalse();
    }

    @Test
    void shouldCheckUserHasAdminPrivileges() {
        UserId adminUserId = UserId.generate();
        Tenant tenant = Tenant.create("Test Tenant", UserId.generate());
        TenantMember adminMember = TenantMember.create(tenant.getId(), adminUserId, io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole.ADMIN);

        boolean hasPrivileges = tenant.hasAdminPrivileges(adminUserId, List.of(adminMember));

        assertThat(hasPrivileges).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNoAdminPrivileges() {
        UserId userId = UserId.generate();
        Tenant tenant = Tenant.create("Test Tenant", UserId.generate());
        TenantMember member = TenantMember.create(tenant.getId(), userId, io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole.MEMBER);

        boolean hasPrivileges = tenant.hasAdminPrivileges(userId, List.of(member));

        assertThat(hasPrivileges).isFalse();
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCodeWhenIdMatches() {
        String name = "Test Tenant";
        UserId ownerUserId = UserId.generate();

        Tenant tenant1 = Tenant.create(name, ownerUserId);
        Tenant tenant2 = Tenant.create(name, ownerUserId);

        assertThat(tenant1).isNotEqualTo(tenant2);
        assertThat(tenant1.hashCode()).isNotEqualTo(tenant2.hashCode());
    }
}