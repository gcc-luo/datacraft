package io.datacraft.execution.application;

import io.datacraft.execution.domain.ExecutionEngine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Collections;

@Component
public class EngineRegistry {
    private final Map<String, ExecutionEngine> engines;

    public EngineRegistry(List<ExecutionEngine> engines) {
        TreeMap<String, ExecutionEngine> registered = new TreeMap<>();
        for (ExecutionEngine engine : engines == null ? List.<ExecutionEngine>of() : engines) {
            Objects.requireNonNull(engine, "engine must not be null");
            String code = engine.engineType();
            if (code == null || code.isBlank()) {
                throw new EngineRegistryException("引擎编码不能为空");
            }
            if (registered.putIfAbsent(code, engine) != null) {
                throw new EngineRegistryException("引擎编码重复: " + code);
            }
        }
        this.engines = Collections.unmodifiableMap(new TreeMap<>(registered));
    }

    public ExecutionEngine get(String engineType) {
        ExecutionEngine engine = engines.get(engineType);
        if (engine == null) {
            throw new EngineNotFoundException(engineType);
        }
        return engine;
    }

    public List<ExecutionEngine> list() {
        return engines.values().stream().toList();
    }
}
