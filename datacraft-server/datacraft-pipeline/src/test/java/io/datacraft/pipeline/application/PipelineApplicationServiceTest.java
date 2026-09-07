package io.datacraft.pipeline.application;

import io.datacraft.api.pipeline.ExecutionStrategy;
import io.datacraft.api.pipeline.PipelineNodeRequest;
import io.datacraft.api.pipeline.PipelineRequest;
import io.datacraft.api.pipeline.PipelineStatus;
import io.datacraft.pipeline.domain.Pipeline;
import io.datacraft.pipeline.domain.PipelineEdge;
import io.datacraft.pipeline.domain.PipelineNode;
import io.datacraft.pipeline.domain.PipelineRepository;
import io.datacraft.pipeline.domain.PipelineValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineApplicationServiceTest {
    @Mock
    private PipelineRepository repository;
    @Mock
    private NodeRegistry nodeRegistry;
    @Mock
    private PipelineValidator validator;
    @InjectMocks
    private PipelineApplicationService service;

    @Test
    void createsDraftPipelineWithAutomaticStrategyAndPersistsGraph() {
        when(nodeRegistry.types()).thenReturn(java.util.Set.of("FILTER"));
        when(repository.save(any(Pipeline.class))).thenAnswer(invocation -> {
            Pipeline value = invocation.getArgument(0);
            return new Pipeline(7L, value.name(), value.description(), value.status(), value.version(),
                    value.executionStrategy(), value.graphJson(), value.createdBy(), value.createdAt(), value.updatedAt(),
                    value.nodes(), value.edges());
        });

        PipelineRequest request = new PipelineRequest("filter customers", null, null, null,
                List.of(new PipelineNodeRequest("filter", "FILTER", "Filter", 1D, 2D, null, null)), List.of());

        var response = service.create(request);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo(PipelineStatus.DRAFT);
        assertThat(response.executionStrategy()).isEqualTo(ExecutionStrategy.AUTO);
        assertThat(response.version()).isEqualTo(1);
        verify(repository).replaceGraph(7L, responseNodes("filter"), List.of());
    }

    @Test
    void updatesPipelineAndIncrementsVersion() {
        Instant createdAt = Instant.parse("2026-09-07T00:00:00Z");
        Pipeline existing = new Pipeline(7L, "old", null, PipelineStatus.DRAFT, 3, ExecutionStrategy.AUTO,
                "{}", 1L, createdAt, createdAt, List.of(), List.of());
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(nodeRegistry.types()).thenReturn(java.util.Set.of("FILTER"));
        when(repository.save(any(Pipeline.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(7L, new PipelineRequest("new", "desc", PipelineStatus.ACTIVE,
                ExecutionStrategy.NATIVE, List.of(), List.of()));

        assertThat(response.version()).isEqualTo(4);
        assertThat(response.status()).isEqualTo(PipelineStatus.ACTIVE);
        assertThat(response.executionStrategy()).isEqualTo(ExecutionStrategy.NATIVE);
        verify(repository).replaceGraph(7L, List.of(), List.of());
    }

    @Test
    void refusesUpdateWhenPipelineDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new PipelineRequest("x", null, null, null, null, null)))
                .isInstanceOf(PipelineNotFoundException.class);
        verify(repository, never()).save(any());
    }

    private static List<PipelineNode> responseNodes(String key) {
        return List.of(new PipelineNode(null, key, "FILTER", "Filter", 1D, 2D, "{}", null));
    }
}
