package io.datacraft.pipeline.application;

public class PipelineNotFoundException extends RuntimeException {
    public PipelineNotFoundException(Long id) {
        super("Pipeline 不存在: " + id);
    }
}
