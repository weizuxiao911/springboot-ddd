package io.github.weizuxiao911.springboot.ddd.common.pbac.domain;

import java.util.HashMap;
import java.util.Map;

public class AccessContext {

    private UserPermissionContext userContext;
    private Map<String, Object> variables;

    public AccessContext() {
        this.variables = new HashMap<>();
    }

    public AccessContext(UserPermissionContext userContext) {
        this.userContext = userContext;
        this.variables = new HashMap<>();
    }

    public UserPermissionContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserPermissionContext userContext) {
        this.userContext = userContext;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Object get(String key) {
        return variables.get(key);
    }

    public void putVariable(String key, Object value) {
        variables.put(key, value);
    }

    public void putAllVariables(Map<String, Object> variables) {
        if (variables != null) {
            this.variables.putAll(variables);
        }
    }
}
