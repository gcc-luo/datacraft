package com.datacraft.auth.web;

import com.datacraft.auth.application.AuthenticationFailureException;
import com.datacraft.auth.application.SystemAdminException;
import com.datacraft.common.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> accessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure("SYSTEM_ADMIN_FORBIDDEN", "没有执行此操作的权限"));
    }

    @ExceptionHandler(SystemAdminException.class)
    public ResponseEntity<ApiResponse<Void>> systemAdminFailure(SystemAdminException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(AuthenticationFailureException.class)
    public ResponseEntity<ApiResponse<Void>> authenticationFailure(AuthenticationFailureException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("AUTH_INVALID", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validationFailure(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getDefaultMessage() == null ? "请求参数无效" : error.getDefaultMessage())
                .orElse("请求参数无效");
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_ERROR", message));
    }
}
