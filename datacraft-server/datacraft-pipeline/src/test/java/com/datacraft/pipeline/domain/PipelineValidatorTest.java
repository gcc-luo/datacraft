package com.datacraft.pipeline.domain;

import com.datacraft.pipeline.application.PipelineValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PipelineValidatorTest {
    private final PipelineValidator validator = new PipelineValidator();
    private final Set<String> knownTypes = Set.of("DATABASE_SOURCE", "FILTER", "DATABASE_SINK");

    @Test
    void acceptsEmptyGraphAndValidDag() {
        assertDoesNotThrow(() -> validator.validate("customer sync", List.of(), List.of(), knownTypes));
        assertDoesNotThrow(() -> validator.validate("customer sync",
                List.of(node("source", "DATABASE_SOURCE"), node("sink", "DATABASE_SINK")),
                List.of(edge("source", "sink")), knownTypes));
    }

    @Test
    void rejectsDuplicateKeysUnknownTypesAndDanglingEdges() {
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node("same", "FILTER"), node("same", "FILTER")), List.of(), knownTypes));
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node("unknown", "NOT_A_NODE")), List.of(), knownTypes));
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node("source", "DATABASE_SOURCE")), List.of(edge("source", "missing")), knownTypes));
    }

    @Test
    void rejectsSelfLoopDuplicateEdgeAndCycle() {
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node("a", "FILTER")), List.of(edge("a", "a")), knownTypes));
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node("a", "FILTER"), node("b", "FILTER")),
                List.of(edge("a", "b"), edge("a", "b")), knownTypes));
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node("a", "FILTER"), node("b", "FILTER")),
                List.of(edge("a", "b"), edge("b", "a")), knownTypes));
    }

    @Test
    void rejectsBlankPipelineNameAndBlankNodeKey() {
        assertThrows(PipelineValidationException.class, () -> validator.validate(" ", List.of(), List.of(), knownTypes));
        assertThrows(PipelineValidationException.class, () -> validator.validate("x",
                List.of(node(" ", "FILTER")), List.of(), knownTypes));
    }

    private static PipelineNode node(String key, String type) {
        return new PipelineNode(null, key, type, key, 0D, 0D, "{}", null);
    }

    private static PipelineEdge edge(String source, String target) {
        return new PipelineEdge(null, source, target, "out", "in", null);
    }
}
