package com.datacraft.quality.web;

import com.datacraft.common.web.ApiResponse;
import com.datacraft.quality.application.QualityExecutionException;
import com.datacraft.quality.application.QualityResultNotFoundException;
import com.datacraft.quality.application.QualityRuleNotFoundException;
import com.datacraft.quality.application.QualityValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class QualityExceptionHandler {
    @ExceptionHandler(QualityValidationException.class)
    public ResponseEntity<ApiResponse<Void>> validation(QualityValidationException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure("QUALITY_VALIDATION", exception.getMessage()));
    }

    @ExceptionHandler(QualityRuleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> ruleNotFound(QualityRuleNotFoundException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure("QUALITY_RULE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(QualityResultNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> resultNotFound(QualityResultNotFoundException exception) {
        return ResponseEntity.status(404).body(ApiResponse.failure("QUALITY_RESULT_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(QualityExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> execution(QualityExecutionException exception) {
        return ResponseEntity.internalServerError().body(ApiResponse.failure("QUALITY_EXECUTION_FAILED", exception.getMessage()));
    }
}
