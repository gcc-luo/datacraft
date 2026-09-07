package com.datacraft.execution.nativeengine;

import com.datacraft.api.execution.EngineDeploymentMode;
import com.datacraft.execution.domain.EngineHealth;
import com.datacraft.execution.domain.EngineHealthStatus;
import com.datacraft.execution.domain.EngineMetadata;
import com.datacraft.execution.domain.ExecutionEngine;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class NativeExecutionEngine implements ExecutionEngine {
    private static final EngineMetadata METADATA = new EngineMetadata(
            "NATIVE", "DataCraft Native", EngineDeploymentMode.EMBEDDED, true,
            List.of("JDBC_SOURCE", "JDBC_SINK", "SQL_PUSHDOWN", "FILTER"));

    @Override
    public String engineType() {
        return METADATA.code();
    }

    @Override
    public EngineMetadata metadata() {
        return METADATA;
    }

    @Override
    public EngineHealth healthCheck() {
        return new EngineHealth(EngineHealthStatus.UP, "Native 引擎已加载", Instant.now());
    }
}
