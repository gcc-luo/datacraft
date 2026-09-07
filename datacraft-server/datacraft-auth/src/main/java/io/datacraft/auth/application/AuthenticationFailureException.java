package io.datacraft.auth.application;

public class AuthenticationFailureException extends RuntimeException {
    public AuthenticationFailureException() {
        super("用户名或密码错误");
    }
}
