package com.datacraft.pipeline.web;

import com.datacraft.common.web.ApiResponse;
import com.datacraft.pipeline.application.NodeTypeNotFoundException;
import com.datacraft.pipeline.application.PipelineNotFoundException;
import com.datacraft.pipeline.application.PipelineValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PipelineExceptionHandler {
    @ExceptionHandler(PipelineNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(PipelineNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("PIPELINE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(NodeTypeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> nodeTypeNotFound(NodeTypeNotFoundException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("NODE_TYPE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(PipelineValidationException.class)
    public ResponseEntity<ApiResponse<Void>> validation(PipelineValidationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("PIPELINE_VALIDATION", exception.getMessage()));
    }
}
