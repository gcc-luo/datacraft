package com.datacraft.execution.domain;

/**
 * Stable extension point between the Control Plane and an execution engine.
 * Planning and runtime operations are added in their dedicated phases.
 */
public interface ExecutionEngine {
    String engineType();

    EngineMetadata metadata();

    EngineHealth healthCheck();
}
