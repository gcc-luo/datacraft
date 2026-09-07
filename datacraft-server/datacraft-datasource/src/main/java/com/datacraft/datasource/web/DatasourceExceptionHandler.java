package com.datacraft.datasource.web;

import com.datacraft.common.web.ApiResponse;
import com.datacraft.datasource.application.DatasourceDuplicateException;
import com.datacraft.datasource.application.DatasourceNotFoundException;
import com.datacraft.datasource.application.DatasourceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DatasourceExceptionHandler {
    @ExceptionHandler(DatasourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(DatasourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("DATASOURCE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DatasourceDuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> duplicate(DatasourceDuplicateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("DATASOURCE_DUPLICATE", exception.getMessage()));
    }

    @ExceptionHandler(DatasourceValidationException.class)
    public ResponseEntity<ApiResponse<Void>> validation(DatasourceValidationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("DATASOURCE_VALIDATION", exception.getMessage()));
    }
}
