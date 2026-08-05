package io.github.weizuxiao911.springboot.ddd.domain.tenant.entity;

import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantId;
import io.github.weizuxiao911.springboot.ddd.domain.tenant.vo.TenantRole;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantMemberTest {

    @Test
    void shouldCreateTenantMember() {
        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();
        TenantRole role = TenantRole.MEMBER;

        TenantMember member = TenantMember.create(tenantId, userId, role);

        assertThat(member.getId()).isNotNull();
        assertThat(member.getTenantId()).isEqualTo(tenantId);
        assertThat(member.getUserId()).isEqualTo(userId);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getDomainEvents()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenTenantIdIsNull() {
        UserId userId = UserId.generate();
        TenantRole role = TenantRole.MEMBER;

        assertThatThrownBy(() -> TenantMember.create(null, userId, role))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TenantId cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        TenantId tenantId = TenantId.generate();
        TenantRole role = TenantRole.MEMBER;

        assertThatThrownBy(() -> TenantMember.create(tenantId, null, role))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UserId cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {
        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();

        assertThatThrownBy(() -> TenantMember.create(tenantId, userId, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role cannot be null");
    }

    @Test
    void shouldPromoteToAdmin() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.MEMBER
        );

        member.promoteToAdmin();

        assertThat(member.getRole()).isEqualTo(TenantRole.ADMIN);
    }

    @Test
    void shouldThrowExceptionWhenPromotingOwnerToAdmin() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.OWNER
        );

        assertThatThrownBy(() -> member.promoteToAdmin())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot change owner role");

        assertThat(member.getRole()).isEqualTo(TenantRole.OWNER);
    }

    @Test
    void shouldDemoteToMember() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.ADMIN
        );

        member.demoteToMember();

        assertThat(member.getRole()).isEqualTo(TenantRole.MEMBER);
    }

    @Test
    void shouldThrowExceptionWhenDemotingOwnerToMember() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.OWNER
        );

        assertThatThrownBy(() -> member.demoteToMember())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot change owner role");

        assertThat(member.getRole()).isEqualTo(TenantRole.OWNER);
    }

    @Test
    void shouldReturnTrueWhenIsOwner() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.OWNER
        );

        assertThat(member.isOwner()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsNotOwner() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.MEMBER
        );

        assertThat(member.isOwner()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenHasAdminPrivileges() {
        TenantMember ownerMember = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.OWNER
        );

        TenantMember adminMember = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.ADMIN
        );

        assertThat(ownerMember.hasAdminPrivileges()).isTrue();
        assertThat(adminMember.hasAdminPrivileges()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenHasNoAdminPrivileges() {
        TenantMember member = TenantMember.create(
                TenantId.generate(),
                UserId.generate(),
                TenantRole.MEMBER
        );

        assertThat(member.hasAdminPrivileges()).isFalse();
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCodeWhenIdsDiffer() {
        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();

        TenantMember member1 = TenantMember.create(tenantId, userId, TenantRole.MEMBER);
        TenantMember member2 = TenantMember.create(tenantId, userId, TenantRole.MEMBER);

        assertThat(member1).isNotEqualTo(member2);
        assertThat(member1.hashCode()).isNotEqualTo(member2.hashCode());
    }
}