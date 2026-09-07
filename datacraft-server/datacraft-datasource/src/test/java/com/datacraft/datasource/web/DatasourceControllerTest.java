package com.datacraft.datasource.web;

import com.datacraft.api.datasource.DatasourceResponse;
import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.application.DatasourceApplicationService;
import com.datacraft.datasource.application.DatasourceDuplicateException;
import com.datacraft.datasource.application.DatasourceNotFoundException;
import com.datacraft.auth.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({DatasourceController.class, DatasourceExceptionHandler.class, ApiExceptionHandler.class,
        DatasourceControllerTest.TestSecurityConfiguration.class})
class DatasourceControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean DatasourceApplicationService service;

    @Test
    void rejectsAnonymousDatasourceRequests() throws Exception {
        mockMvc.perform(get("/api/v1/datasources")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void returnsPasswordFreeDatasourceResponse() throws Exception {
        when(service.list()).thenReturn(List.of(new DatasourceResponse(1L, "warehouse", DatasourceType.POSTGRESQL,
                "localhost", 5432, "datacraft", "reader", null, DatasourceStatus.UNKNOWN, null, null, null,
                Instant.parse("2026-09-07T02:00:00Z"), Instant.parse("2026-09-07T02:00:00Z"))));

        mockMvc.perform(get("/api/v1/datasources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("warehouse"))
                .andExpect(jsonPath("$.data[0].password").doesNotExist())
                .andExpect(jsonPath("$.data[0].passwordCiphertext").doesNotExist());
    }

    @Test
    @WithMockUser
    void validatesCreateRequest() throws Exception {
        mockMvc.perform(post("/api/v1/datasources")
                        .contentType("application/json")
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser
    void mapsDomainNotFoundAndDuplicateErrors() throws Exception {
        when(service.get(404L)).thenThrow(new DatasourceNotFoundException(404L));
        when(service.create(org.mockito.ArgumentMatchers.any())).thenThrow(new DatasourceDuplicateException("warehouse"));

        mockMvc.perform(get("/api/v1/datasources/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DATASOURCE_NOT_FOUND"));
        mockMvc.perform(post("/api/v1/datasources")
                        .contentType("application/json")
                        .content("{\"name\":\"warehouse\",\"type\":\"POSTGRESQL\",\"host\":\"localhost\",\"port\":5432,\"databaseName\":\"db\",\"username\":\"u\",\"password\":\"p\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATASOURCE_DUPLICATE"));
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
