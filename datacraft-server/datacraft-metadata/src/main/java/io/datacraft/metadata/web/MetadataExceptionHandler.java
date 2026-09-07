package io.datacraft.metadata.web;

import io.datacraft.common.web.ApiResponse;
import io.datacraft.datasource.application.DatasourceNotFoundException;
import io.datacraft.metadata.application.DatasetNotFoundException;
import io.datacraft.metadata.application.MetadataCollectionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MetadataExceptionHandler {
    @ExceptionHandler(DatasetNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> datasetNotFound(DatasetNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("DATASET_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DatasourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> datasourceNotFound(DatasourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("DATASOURCE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(MetadataCollectionException.class)
    public ResponseEntity<ApiResponse<Void>> collectionFailure(MetadataCollectionException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.failure("METADATA_SYNC_FAILED", "元数据同步失败，请检查数据源配置与网络"));
    }
}
