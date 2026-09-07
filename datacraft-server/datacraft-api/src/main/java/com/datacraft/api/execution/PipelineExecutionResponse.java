package com.datacraft.api.execution;

public record PipelineExecutionResponse(long inputRows, long outputRows, String status) {
}
