package com.datacraft.execution.web;

import com.datacraft.api.execution.ExecutionPlanResponse;
import com.datacraft.api.execution.ExecutionStageResponse;
import com.datacraft.api.execution.DataReferenceResponse;
import com.datacraft.api.execution.PipelineExecutionResponse;
import com.datacraft.common.web.ApiResponse;
import com.datacraft.execution.application.ExecutionPlanner;
import com.datacraft.execution.application.ExecutionPlanningException;
import com.datacraft.execution.domain.DataReference;
import com.datacraft.execution.domain.ExecutionPlan;
import com.datacraft.execution.domain.ExecutionStagePlan;
import com.datacraft.execution.nativeengine.NativeExecutionException;
import com.datacraft.execution.nativeengine.NativeExecutionResult;
import com.datacraft.execution.nativeengine.NativePipelineExecutor;
import com.datacraft.pipeline.application.PipelineNotFoundException;
import com.datacraft.pipeline.application.PipelineValidationException;
import com.datacraft.pipeline.domain.Pipeline;
import com.datacraft.pipeline.domain.PipelineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
public class PipelineExecutionController {
    private final PipelineRepository pipelineRepository;
    private final ExecutionPlanner planner;
    private final NativePipelineExecutor executor;

    public PipelineExecutionController(PipelineRepository pipelineRepository, ExecutionPlanner planner,
                                      NativePipelineExecutor executor) {
        this.pipelineRepository = pipelineRepository;
        this.planner = planner;
        this.executor = executor;
    }

    @PostMapping("/{id}/validate")
    public ApiResponse<Void> validate(@PathVariable Long id) {
        Pipeline pipeline = require(id);
        planner.plan(pipeline);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/plan")
    public ApiResponse<ExecutionPlanResponse> plan(@PathVariable Long id) {
        Pipeline pipeline = require(id);
        ExecutionPlan plan = planner.plan(pipeline);
        return ApiResponse.success(toResponse(plan));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<PipelineExecutionResponse> execute(@PathVariable Long id) {
        Pipeline pipeline = require(id);
        NativeExecutionResult result = executor.execute(pipeline);
        return ApiResponse.success(new PipelineExecutionResponse(
                result.inputRows(), result.outputRows(), "SUCCESS"));
    }

    private Pipeline require(Long id) {
        return pipelineRepository.findById(id)
                .orElseThrow(() -> new PipelineNotFoundException(id));
    }

    private ExecutionPlanResponse toResponse(ExecutionPlan plan) {
        List<ExecutionStageResponse> stages = plan.stages().stream()
                .map(this::toStageResponse)
                .toList();
        return new ExecutionPlanResponse(plan.pipelineId(), stages);
    }

    private ExecutionStageResponse toStageResponse(ExecutionStagePlan stage) {
        List<DataReferenceResponse> inputs = stage.inputReferences().stream()
                .map(this::toRefResponse)
                .toList();
        List<DataReferenceResponse> outputs = stage.outputReferences().stream()
                .map(this::toRefResponse)
                .toList();
        return new ExecutionStageResponse(stage.stageIndex(), stage.engineCode(),
                stage.nodeKeys(), inputs, outputs);
    }

    private DataReferenceResponse toRefResponse(DataReference ref) {
        return new DataReferenceResponse(ref.type().name(), ref.uri());
    }

    @ExceptionHandler(PipelineNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(PipelineNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("PIPELINE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(ExecutionPlanningException.class)
    public ResponseEntity<ApiResponse<Void>> planningError(ExecutionPlanningException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("EXECUTION_PLANNING", exception.getMessage()));
    }

    @ExceptionHandler(PipelineValidationException.class)
    public ResponseEntity<ApiResponse<Void>> validation(PipelineValidationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("PIPELINE_VALIDATION", exception.getMessage()));
    }

    @ExceptionHandler(NativeExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> execution(NativeExecutionException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("NATIVE_EXECUTION", exception.getMessage()));
    }
}
