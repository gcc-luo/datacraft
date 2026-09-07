package com.datacraft.pipeline.web;

import com.datacraft.api.pipeline.ExecutionStrategy;
import com.datacraft.api.pipeline.NodeCategory;
import com.datacraft.api.pipeline.NodeMetadataResponse;
import com.datacraft.api.pipeline.PipelineDetailResponse;
import com.datacraft.api.pipeline.PipelineResponse;
import com.datacraft.api.pipeline.PipelineStatus;
import com.datacraft.auth.web.ApiExceptionHandler;
import com.datacraft.pipeline.application.NodeTypeNotFoundException;
import com.datacraft.pipeline.application.PipelineApplicationService;
import com.datacraft.pipeline.application.PipelineNotFoundException;
import com.datacraft.pipeline.application.PipelineValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({PipelineController.class, PipelineExceptionHandler.class, ApiExceptionHandler.class,
        PipelineControllerTest.TestSecurityConfiguration.class})
class PipelineControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean PipelineApplicationService service;

    @Test
    void rejectsAnonymousPipelineRequests() throws Exception {
        mockMvc.perform(get("/api/v1/pipelines")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void managesPipelineResourcesAndNodeMetadata() throws Exception {
        Instant now = Instant.parse("2026-09-07T03:00:00Z");
        PipelineResponse response = new PipelineResponse(7L, "sync", null, PipelineStatus.DRAFT, 1,
                ExecutionStrategy.AUTO, now, now);
        when(service.list()).thenReturn(List.of(response));
        when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(response);
        when(service.get(7L)).thenReturn(new PipelineDetailResponse(7L, "sync", null, PipelineStatus.DRAFT, 1,
                ExecutionStrategy.AUTO, now, now, List.of(), List.of()));
        when(service.listNodeTypes()).thenReturn(List.of(new NodeMetadataResponse("FILTER", "Filter",
                NodeCategory.TRANSFORM, "filter", List.of("NATIVE"), "NATIVE", Map.of())));
        when(service.getNodeType("FILTER")).thenReturn(new NodeMetadataResponse("FILTER", "Filter",
                NodeCategory.TRANSFORM, "filter", List.of("NATIVE"), "NATIVE", Map.of()));

        mockMvc.perform(get("/api/v1/pipelines"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(7));
        mockMvc.perform(post("/api/v1/pipelines").contentType("application/json")
                        .content("{\"name\":\"sync\",\"nodes\":[],\"edges\":[]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.version").value(1));
        mockMvc.perform(put("/api/v1/pipelines/7").contentType("application/json")
                        .content("{\"name\":\"sync v2\",\"nodes\":[],\"edges\":[]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pipelines/7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.nodes").isArray());
        mockMvc.perform(delete("/api/v1/pipelines/7")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/node-types"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].type").value("FILTER"));
        mockMvc.perform(get("/api/v1/node-types/FILTER"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.category").value("TRANSFORM"));
    }

    @Test
    @WithMockUser
    void mapsPipelineErrorsToStableCodes() throws Exception {
        when(service.get(404L)).thenThrow(new PipelineNotFoundException(404L));
        when(service.getNodeType("NOPE")).thenThrow(new NodeTypeNotFoundException("NOPE"));
        doThrow(new PipelineValidationException("Pipeline 名称不能为空"))
                .when(service).delete(eq(400L));

        mockMvc.perform(get("/api/v1/pipelines/404"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PIPELINE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/node-types/NOPE"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("NODE_TYPE_NOT_FOUND"));
        mockMvc.perform(delete("/api/v1/pipelines/400"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PIPELINE_VALIDATION"));
    }

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .build();
        }
    }
}
