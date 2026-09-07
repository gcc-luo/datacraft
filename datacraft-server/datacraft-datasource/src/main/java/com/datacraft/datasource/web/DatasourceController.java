package com.datacraft.datasource.web;

import com.datacraft.api.datasource.DatasourceRequest;
import com.datacraft.api.datasource.DatasourceResponse;
import com.datacraft.api.datasource.DatasourceTestResponse;
import com.datacraft.common.web.ApiResponse;
import com.datacraft.datasource.application.DatasourceApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datasources")
public class DatasourceController {
    private final DatasourceApplicationService service;

    public DatasourceController(DatasourceApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<DatasourceResponse>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping
    public ApiResponse<DatasourceResponse> create(@Valid @RequestBody DatasourceRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DatasourceResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<DatasourceResponse> update(@PathVariable Long id, @Valid @RequestBody DatasourceRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<DatasourceTestResponse> testConnection(@PathVariable Long id,
                                                               @Valid @RequestBody(required = false) DatasourceRequest request) {
        return ApiResponse.success(service.testConnection(id, request));
    }

    @PostMapping("/test")
    public ApiResponse<DatasourceTestResponse> testConnection(@Valid @RequestBody DatasourceRequest request) {
        return ApiResponse.success(service.testConnection(request));
    }
}
