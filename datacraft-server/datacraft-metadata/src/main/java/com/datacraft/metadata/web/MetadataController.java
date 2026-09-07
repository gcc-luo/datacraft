package com.datacraft.metadata.web;

import com.datacraft.api.metadata.DatasetDetailResponse;
import com.datacraft.api.metadata.DatasetResponse;
import com.datacraft.api.metadata.MetadataSyncResponse;
import com.datacraft.common.web.ApiResponse;
import com.datacraft.metadata.application.MetadataApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MetadataController {
    private final MetadataApplicationService service;

    public MetadataController(MetadataApplicationService service) {
        this.service = service;
    }

    @PostMapping("/datasources/{datasourceId}/metadata/sync")
    public ApiResponse<MetadataSyncResponse> sync(@PathVariable Long datasourceId) {
        return ApiResponse.success(service.sync(datasourceId));
    }

    @GetMapping("/datasets")
    public ApiResponse<List<DatasetResponse>> list(
            @RequestParam(required = false) Long datasourceId,
            @RequestParam(required = false) String schemaName,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(service.list(datasourceId, schemaName, keyword));
    }

    @GetMapping("/datasets/{id}")
    public ApiResponse<DatasetDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.get(id));
    }
}
