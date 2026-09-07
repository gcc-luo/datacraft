package io.datacraft.execution.application;

public class EngineNotFoundException extends RuntimeException {
    public EngineNotFoundException(String engineType) {
        super("引擎不存在: " + engineType);
    }
}
