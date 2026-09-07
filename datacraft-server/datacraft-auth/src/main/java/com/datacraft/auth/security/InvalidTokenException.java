package com.datacraft.auth.security;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(Throwable cause) {
        super("Invalid access token", cause);
    }
}
