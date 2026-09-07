package com.datacraft.execution.application;

public class ExecutionPlanningException extends RuntimeException {
    public ExecutionPlanningException(String message) {
        super(message);
    }

    public ExecutionPlanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
