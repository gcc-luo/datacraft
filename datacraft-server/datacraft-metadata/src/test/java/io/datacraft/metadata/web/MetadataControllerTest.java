package io.datacraft.metadata.web;

import io.datacraft.api.metadata.DatasetDetailResponse;
import io.datacraft.api.metadata.DatasetFieldResponse;
import io.datacraft.api.metadata.DatasetResponse;
import io.datacraft.api.metadata.MetadataSyncResponse;
import io.datacraft.auth.web.ApiExceptionHandler;
import io.datacraft.metadata.application.DatasetNotFoundException;
import io.datacraft.metadata.application.MetadataApplicationService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({MetadataController.class, MetadataExceptionHandler.class, ApiExceptionHandler.class,
        MetadataControllerTest.TestSecurityConfiguration.class})
class MetadataControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean MetadataApplicationService service;

    @Test
    void rejectsAnonymousMetadataRequests() throws Exception {
        mockMvc.perform(get("/api/v1/datasets")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void returnsSyncCountsAndDatasetDetailsWithoutCredentials() throws Exception {
        Instant collectedAt = Instant.parse("2026-09-07T03:00:00Z");
        when(service.sync(1L)).thenReturn(new MetadataSyncResponse(1L, 1, 1, 1, collectedAt));
        when(service.get(9L)).thenReturn(new DatasetDetailResponse(9L, 1L, "db", "public", "customer",
                null, 12L, collectedAt, List.of(new DatasetFieldResponse(1L, "id", 1, "BIGINT", false, true, null))));

        mockMvc.perform(post("/api/v1/datasources/1/metadata/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.datasetCount").value(1))
                .andExpect(jsonPath("$.data.fieldCount").value(1))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordCiphertext").doesNotExist());
        mockMvc.perform(get("/api/v1/datasets/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fields[0].fieldName").value("id"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser
    void forwardsDatasetFiltersAndMapsNotFound() throws Exception {
        when(service.list(eq(1L), eq("public"), eq("phone"))).thenReturn(List.of(
                new DatasetResponse(9L, 1L, "db", "public", "customer", null, 12L,
                        Instant.parse("2026-09-07T03:00:00Z"))));
        when(service.get(404L)).thenThrow(new DatasetNotFoundException(404L));

        mockMvc.perform(get("/api/v1/datasets").param("datasourceId", "1")
                        .param("schemaName", "public").param("keyword", "phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tableName").value("customer"));
        mockMvc.perform(get("/api/v1/datasets/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DATASET_NOT_FOUND"));
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
