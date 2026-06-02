package top.archaiharness.framework.common.pbac.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserPermissionContext {

    private final Long userId;
    private final Long currentTenantId;
    private final Set<Long> accessibleTenants;
    private final Map<Long, Set<PermissionCode>> permissionsByTenant;

    public UserPermissionContext(Long userId, Long currentTenantId, Set<Long> accessibleTenants, Map<Long, Set<PermissionCode>> permissionsByTenant) {
        this.userId = userId;
        this.currentTenantId = currentTenantId;
        this.accessibleTenants = accessibleTenants != null ? accessibleTenants : new HashSet<>();
        this.permissionsByTenant = permissionsByTenant != null ? permissionsByTenant : new HashMap<>();
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCurrentTenantId() {
        return currentTenantId;
    }

    public Set<Long> getAccessibleTenants() {
        return new HashSet<>(accessibleTenants);
    }

    public Map<Long, Set<PermissionCode>> getPermissionsByTenant() {
        return new HashMap<>(permissionsByTenant);
    }

    public boolean hasPermission(PermissionCode code) {
        return hasPermission(currentTenantId, code);
    }

    public boolean hasPermission(Long tenantId, PermissionCode code) {
        if (!accessibleTenants.contains(tenantId)) {
            return false;
        }

        Set<PermissionCode> permissions = permissionsByTenant.get(tenantId);
        return permissions != null && permissions.contains(code);
    }

    public boolean canAccessTenant(Long tenantId) {
        return accessibleTenants.contains(tenantId);
    }
}
