package com.datacraft.auth.application;

public class SystemAdminException extends RuntimeException {
    private final String code;

    public SystemAdminException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
