package com.datacraft.execution.application;

import com.datacraft.execution.domain.EngineHealth;
import com.datacraft.execution.domain.EngineHealthStatus;
import com.datacraft.execution.domain.EngineMetadata;
import com.datacraft.execution.domain.ExecutionEngine;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineRegistryTest {
    @Test
    void listsEnginesInStableCodeOrderAndFindsByCode() {
        ExecutionEngine zeta = engine("ZETA");
        ExecutionEngine nativeEngine = engine("NATIVE");
        EngineRegistry registry = new EngineRegistry(List.of(zeta, nativeEngine));

        assertEquals(List.of("NATIVE", "ZETA"), registry.list().stream()
                .map(ExecutionEngine::engineType).toList());
        assertEquals(nativeEngine, registry.get("NATIVE"));
    }

    @Test
    void rejectsDuplicateAndUnknownEngineCodes() {
        assertThrows(EngineRegistryException.class,
                () -> new EngineRegistry(List.of(engine("NATIVE"), engine("NATIVE"))));
        EngineRegistry registry = new EngineRegistry(List.of(engine("NATIVE")));
        assertThrows(EngineNotFoundException.class, () -> registry.get("DATAX"));
    }

    private ExecutionEngine engine(String code) {
        return new ExecutionEngine() {
            @Override public String engineType() { return code; }
            @Override public EngineMetadata metadata() { return EngineMetadata.test(code); }
            @Override public EngineHealth healthCheck() {
                return new EngineHealth(EngineHealthStatus.UP, "ok", Instant.EPOCH);
            }
        };
    }
}
