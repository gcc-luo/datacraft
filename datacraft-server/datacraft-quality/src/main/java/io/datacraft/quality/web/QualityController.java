package io.datacraft.quality.web;

import io.datacraft.api.quality.QualityCheckRequest;
import io.datacraft.api.quality.QualityResultResponse;
import io.datacraft.common.web.ApiResponse;
import io.datacraft.quality.application.QualityApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quality")
public class QualityController {
    private final QualityApplicationService service;

    public QualityController(QualityApplicationService service) { this.service = service; }

    @PostMapping("/checks")
    public ApiResponse<List<QualityResultResponse>> check(@RequestBody QualityCheckRequest request) {
        return ApiResponse.success(service.check(request));
    }

    @GetMapping("/results")
    public ApiResponse<List<QualityResultResponse>> list() { return ApiResponse.success(service.list()); }

    @GetMapping("/results/{id}")
    public ApiResponse<QualityResultResponse> get(@PathVariable Long id) { return ApiResponse.success(service.get(id)); }
}
