package io.datacraft.execution.nativeengine;

import io.datacraft.execution.domain.EngineHealthStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeExecutionEngineTest {
    private final NativeExecutionEngine engine = new NativeExecutionEngine();

    @Test
    void declaresEmbeddedNativeCapabilities() {
        assertEquals("NATIVE", engine.engineType());
        assertEquals("EMBEDDED", engine.metadata().deploymentMode().name());
        assertTrue(engine.metadata().enabled());
        assertTrue(engine.metadata().capabilities().contains("JDBC_SOURCE"));
        assertTrue(engine.metadata().capabilities().contains("JDBC_SINK"));
        assertTrue(engine.metadata().capabilities().contains("SQL_PUSHDOWN"));
    }

    @Test
    void reportsLoadedHealthWithoutConnectingToDatasource() {
        assertEquals(EngineHealthStatus.UP, engine.healthCheck().status());
    }
}
