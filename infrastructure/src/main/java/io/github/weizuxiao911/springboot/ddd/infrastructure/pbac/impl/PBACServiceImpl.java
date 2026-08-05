package io.github.weizuxiao911.springboot.ddd.infrastructure.pbac.impl;

import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.AccessContext;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.EvaluationResult;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.PermissionCode;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.UserPermissionContext;
import io.github.weizuxiao911.springboot.ddd.common.pbac.service.PBACService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class PBACServiceImpl implements PBACService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ExpressionParser spelParser = new SpelExpressionParser();

    @Override
    public EvaluationResult evaluate(AccessContext context) {
        UserPermissionContext userContext = context.getUserContext();
        String expression = (String) context.get("expression");
        String tenantIdExpression = (String) context.get("tenantIdExpression");

        if (expression == null || expression.trim().isEmpty()) {
            return EvaluationResult.deny("NO_PERMISSION_CODE");
        }

        String requiredPermissionCode = parseExpression(expression, context.getVariables());

        Long tenantId = userContext.getCurrentTenantId();
        if (tenantIdExpression != null && !tenantIdExpression.trim().isEmpty()) {
            try {
                String tenantIdStr = parseExpression(tenantIdExpression, context.getVariables());
                if (tenantIdStr != null && !tenantIdStr.trim().isEmpty()) {
                    tenantId = Long.valueOf(tenantIdStr);
                }
            } catch (Exception e) {
                return EvaluationResult.deny("INVALID_TENANT_ID");
            }
        }

        PermissionCode requiredCode = PermissionCode.of(requiredPermissionCode);

        if (userContext.hasPermission(tenantId, requiredCode)) {
            return EvaluationResult.allow(requiredPermissionCode);
        } else {
            return EvaluationResult.deny(requiredPermissionCode);
        }
    }

    @Override
    public UserPermissionContext parseUserContext(Map<String, String> headers) {
        String userIdStr = headers.get("x-user-id");
        String tenantIdStr = headers.get("x-tenant-id");
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new IllegalArgumentException("Missing required header: x-user-id");
        }
        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            throw new IllegalArgumentException("Missing required header: x-tenant-id");
        }

        Long userId = Long.valueOf(userIdStr.trim());
        Long currentTenantId = Long.valueOf(tenantIdStr.trim());
        String accessibleTenantsStr = headers.get("x-accessible-tenants");
        String tenantPermissionsJson = headers.get("x-tenant-permissions");

        Set<Long> accessibleTenants = new HashSet<>();
        if (accessibleTenantsStr != null && !accessibleTenantsStr.trim().isEmpty()) {
            Arrays.stream(accessibleTenantsStr.split(","))
                .filter(s -> !s.trim().isEmpty())
                .map(String::trim)
                .map(Long::valueOf)
                .forEach(accessibleTenants::add);
        }

        Map<Long, Set<PermissionCode>> permissionsByTenant = new HashMap<>();
        if (tenantPermissionsJson != null && !tenantPermissionsJson.trim().isEmpty()) {
            try {
                Map<String, List<String>> permissionsMap = objectMapper.readValue(
                    tenantPermissionsJson,
                    new TypeReference<Map<String, List<String>>>() {}
                );

                for (Map.Entry<String, List<String>> entry : permissionsMap.entrySet()) {
                    Long tenantId = Long.valueOf(entry.getKey());
                    Set<PermissionCode> permissions = entry.getValue().stream()
                        .map(PermissionCode::of)
                        .collect(Collectors.toSet());
                    permissionsByTenant.put(tenantId, permissions);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse tenant permissions", e);
            }
        }

        return new UserPermissionContext(userId, currentTenantId, accessibleTenants, permissionsByTenant);
    }

    private String parseExpression(@NonNull String expression, Map<String, Object> variables) {
        if (!expression.contains("#") && !expression.contains("?")) {
            return expression;
        }

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        if (variables != null) {
            variables.forEach(evalContext::setVariable);
        }

        Expression exp = spelParser.parseExpression(expression);
        Object result = exp.getValue(evalContext);

        return result != null ? result.toString() : null;
    }
}
