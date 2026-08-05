package io.github.weizuxiao911.springboot.ddd.common.pbac.domain;

public class EvaluationResult {

    private final boolean allowed;
    private final String permissionCode;

    private EvaluationResult(boolean allowed, String permissionCode) {
        this.allowed = allowed;
        this.permissionCode = permissionCode;
    }

    public static EvaluationResult allow(String permissionCode) {
        return new EvaluationResult(true, permissionCode);
    }

    public static EvaluationResult deny(String permissionCode) {
        return new EvaluationResult(false, permissionCode);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getPermissionCode() {
        return permissionCode;
    }
}
