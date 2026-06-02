package top.archaiharness.framework.common.pbac.domain;

import java.util.Objects;

public class PermissionCode {

    private final String code;

    public PermissionCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission code cannot be null or empty");
        }
        this.code = code.trim();
    }

    public String getCode() {
        return code;
    }

    public static PermissionCode of(String code) {
        return new PermissionCode(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionCode that = (PermissionCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
