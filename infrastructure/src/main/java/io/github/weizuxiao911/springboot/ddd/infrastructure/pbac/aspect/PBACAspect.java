package io.github.weizuxiao911.springboot.ddd.infrastructure.pbac.aspect;

import io.github.weizuxiao911.springboot.ddd.common.annotation.RequirePermission;
import io.github.weizuxiao911.springboot.ddd.common.annotation.RequirePolicy;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.AccessContext;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.EvaluationResult;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.UserPermissionContext;
import io.github.weizuxiao911.springboot.ddd.common.pbac.exception.AccessDeniedException;
import io.github.weizuxiao911.springboot.ddd.common.pbac.service.PBACService;
import io.github.weizuxiao911.springboot.ddd.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@RequiredArgsConstructor
public class PBACAspect {

    private final PBACService pbacService;

    private final ExpressionParser spelParser = new SpelExpressionParser();

    @Around("@annotation(requirePolicy)")
    public Object aroundRequirePolicy(ProceedingJoinPoint joinPoint, RequirePolicy requirePolicy) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        Map<String, String> headers = getHeaders(request);
        UserPermissionContext userContext = pbacService.parseUserContext(headers);

        Map<String, Object> variables = buildVariables(joinPoint, request);

        AccessContext context = new AccessContext(userContext);
        context.putAllVariables(variables);
        context.putVariable("expression", requirePolicy.permissionCode());

        String tenantIdExpression = requirePolicy.tenantId();
        if (tenantIdExpression != null && !tenantIdExpression.trim().isEmpty()) {
            context.putVariable("tenantIdExpression", tenantIdExpression);
        }

        EvaluationResult result = pbacService.evaluate(context);

        if (!result.isAllowed()) {
            throw new AccessDeniedException(result.getPermissionCode());
        }

        return joinPoint.proceed();
    }

    @SuppressWarnings("null")
    @Around("@annotation(requirePermission)")
    public Object aroundRequirePermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        Map<String, String> headers = getHeaders(request);
        UserPermissionContext userContext = pbacService.parseUserContext(headers);

        Map<String, Object> variables = buildVariables(joinPoint, request);

        String permissionCode = resolveExpression(requirePermission.resource(), variables) + ":" + requirePermission.action();

        AccessContext context = new AccessContext(userContext);
        context.putAllVariables(variables);
        context.putVariable("expression", permissionCode);

        EvaluationResult result = pbacService.evaluate(context);

        if (!result.isAllowed()) {
            throw new AccessDeniedException(result.getPermissionCode());
        }

        if (!requirePermission.condition().isEmpty()) {
            Boolean conditionMet = resolveExpression(requirePermission.condition(), variables, Boolean.class);
            if (conditionMet == null || !conditionMet) {
                throw new AccessDeniedException(permissionCode);
            }
        }

        return joinPoint.proceed();
    }

    private String resolveExpression(@NonNull String expression, Map<String, Object> variables) {
        return resolveExpression(expression, variables, String.class);
    }

    private <T> T resolveExpression(@NonNull String expression, Map<String, Object> variables, Class<T> resultType) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        if (variables != null) {
            variables.forEach(evalContext::setVariable);
        }
        Expression exp = spelParser.parseExpression(expression);
        return exp.getValue(evalContext, resultType);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No current HTTP request");
        }
        return attributes.getRequest();
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }

        return headers;
    }

    private Map<String, Object> buildVariables(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        Map<String, Object> variables = new HashMap<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                variables.put(paramNames[i], args[i]);
            }
        }

        if (UserContext.getUser() != null) {
            variables.put("userContext", UserContext.getContext());
        }

        variables.put("request", request);

        return variables;
    }
}
