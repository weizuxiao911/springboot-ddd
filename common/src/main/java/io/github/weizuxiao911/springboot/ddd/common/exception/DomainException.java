package io.github.weizuxiao911.springboot.ddd.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public DomainException(String message) {
        super(message);
        this.code = "DOMAIN_ERROR";
    }

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static DomainException of(String message) {
        return new DomainException(message);
    }

    public static DomainException of(String code, String message) {
        return new DomainException(code, message);
    }

    public static DomainException notFound(String entityName, String id) {
        return new DomainException("NOT_FOUND", entityName + " not found: " + id);
    }

    public static DomainException alreadyExists(String entityName, String identifier) {
        return new DomainException("ALREADY_EXISTS", entityName + " already exists: " + identifier);
    }

    public static DomainException invalidState(String message) {
        return new DomainException("INVALID_STATE", message);
    }
}
