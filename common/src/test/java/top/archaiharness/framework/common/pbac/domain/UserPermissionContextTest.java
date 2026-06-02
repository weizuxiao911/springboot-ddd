package top.archaiharness.framework.common.pbac.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserPermissionContextTest {

    @Test
    void shouldCreateContextWithAllParameters() {
        Long userId = 1L;
        Long currentTenantId = 100L;
        Set<Long> accessibleTenants = new HashSet<>(Set.of(100L, 200L));
        Map<Long, Set<PermissionCode>> permissionsByTenant = new HashMap<>();
        permissionsByTenant.put(100L, new HashSet<>(Set.of(
                PermissionCode.of("USER:READ"),
                PermissionCode.of("USER:WRITE")
        )));

        UserPermissionContext context = new UserPermissionContext(
                userId, currentTenantId, accessibleTenants, permissionsByTenant);

        assertThat(context.getUserId()).isEqualTo(userId);
        assertThat(context.getCurrentTenantId()).isEqualTo(currentTenantId);
        assertThat(context.getAccessibleTenants()).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void shouldHandleNullAccessibleTenants() {
        UserPermissionContext context = new UserPermissionContext(1L, 100L, null, null);

        assertThat(context.getAccessibleTenants()).isEmpty();
    }

    @Test
    void shouldHandleNullPermissionsByTenant() {
        UserPermissionContext context = new UserPermissionContext(1L, 100L, null, null);

        assertThat(context.getPermissionsByTenant()).isEmpty();
    }

    @Test
    void shouldCheckPermissionInCurrentTenant() {
        Set<Long> accessibleTenants = new HashSet<>(Set.of(100L));
        Map<Long, Set<PermissionCode>> permissionsByTenant = new HashMap<>();
        permissionsByTenant.put(100L, new HashSet<>(Set.of(
                PermissionCode.of("USER:READ")
        )));

        UserPermissionContext context = new UserPermissionContext(
                1L, 100L, accessibleTenants, permissionsByTenant);

        assertThat(context.hasPermission(PermissionCode.of("USER:READ"))).isTrue();
        assertThat(context.hasPermission(PermissionCode.of("USER:WRITE"))).isFalse();
    }

    @Test
    void shouldCheckPermissionInSpecificTenant() {
        Set<Long> accessibleTenants = new HashSet<>(Set.of(100L, 200L));
        Map<Long, Set<PermissionCode>> permissionsByTenant = new HashMap<>();
        permissionsByTenant.put(100L, new HashSet<>(Set.of(
                PermissionCode.of("USER:READ")
        )));
        permissionsByTenant.put(200L, new HashSet<>(Set.of(
                PermissionCode.of("USER:WRITE")
        )));

        UserPermissionContext context = new UserPermissionContext(
                1L, 100L, accessibleTenants, permissionsByTenant);

        assertThat(context.hasPermission(100L, PermissionCode.of("USER:READ"))).isTrue();
        assertThat(context.hasPermission(100L, PermissionCode.of("USER:WRITE"))).isFalse();
        assertThat(context.hasPermission(200L, PermissionCode.of("USER:READ"))).isFalse();
        assertThat(context.hasPermission(200L, PermissionCode.of("USER:WRITE"))).isTrue();
    }

    @Test
    void shouldDenyPermissionForInaccessibleTenant() {
        Set<Long> accessibleTenants = new HashSet<>(Set.of(100L));
        Map<Long, Set<PermissionCode>> permissionsByTenant = new HashMap<>();

        UserPermissionContext context = new UserPermissionContext(
                1L, 100L, accessibleTenants, permissionsByTenant);

        assertThat(context.hasPermission(200L, PermissionCode.of("USER:READ"))).isFalse();
    }

    @Test
    void shouldCheckAccessToTenant() {
        Set<Long> accessibleTenants = new HashSet<>(Set.of(100L, 200L, 300L));

        UserPermissionContext context = new UserPermissionContext(
                1L, 100L, accessibleTenants, null);

        assertThat(context.canAccessTenant(100L)).isTrue();
        assertThat(context.canAccessTenant(200L)).isTrue();
        assertThat(context.canAccessTenant(300L)).isTrue();
        assertThat(context.canAccessTenant(400L)).isFalse();
    }

    @Test
    void shouldReturnDefensiveCopies() {
        Set<Long> accessibleTenants = new HashSet<>(Set.of(100L));
        Map<Long, Set<PermissionCode>> permissionsByTenant = new HashMap<>();
        permissionsByTenant.put(100L, new HashSet<>(Set.of(
                PermissionCode.of("USER:READ")
        )));

        UserPermissionContext context = new UserPermissionContext(
                1L, 100L, accessibleTenants, permissionsByTenant);

        Set<Long> tenants = context.getAccessibleTenants();
        tenants.add(999L);

        assertThat(context.getAccessibleTenants()).doesNotContain(999L);

        Map<Long, Set<PermissionCode>> permissions = context.getPermissionsByTenant();
        permissions.put(999L, new HashSet<>());

        assertThat(context.getPermissionsByTenant()).doesNotContainKey(999L);
    }
}