package io.datacraft.pipeline.web;

import io.datacraft.api.pipeline.NodeMetadataResponse;
import io.datacraft.api.pipeline.PipelineDetailResponse;
import io.datacraft.api.pipeline.PipelineRequest;
import io.datacraft.api.pipeline.PipelineResponse;
import io.datacraft.common.web.ApiResponse;
import io.datacraft.pipeline.application.PipelineApplicationService;
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
@RequestMapping("/api/v1")
public class PipelineController {
    private final PipelineApplicationService service;

    public PipelineController(PipelineApplicationService service) {
        this.service = service;
    }

    @GetMapping("/pipelines")
    public ApiResponse<List<PipelineResponse>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping("/pipelines")
    public ApiResponse<PipelineResponse> create(@RequestBody PipelineRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @GetMapping("/pipelines/{id}")
    public ApiResponse<PipelineDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.get(id));
    }

    @PutMapping("/pipelines/{id}")
    public ApiResponse<PipelineResponse> update(@PathVariable Long id, @RequestBody PipelineRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/pipelines/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/node-types")
    public ApiResponse<List<NodeMetadataResponse>> listNodeTypes() {
        return ApiResponse.success(service.listNodeTypes());
    }

    @GetMapping("/node-types/{type}")
    public ApiResponse<NodeMetadataResponse> getNodeType(@PathVariable String type) {
        return ApiResponse.success(service.getNodeType(type));
    }
}
