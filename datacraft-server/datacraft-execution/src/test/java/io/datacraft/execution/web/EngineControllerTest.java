package io.datacraft.execution.web;

import io.datacraft.auth.web.ApiExceptionHandler;
import io.datacraft.execution.application.EngineRegistry;
import io.datacraft.execution.domain.EngineHealth;
import io.datacraft.execution.domain.EngineHealthStatus;
import io.datacraft.execution.domain.EngineMetadata;
import io.datacraft.execution.domain.ExecutionEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({EngineController.class, EngineControllerTest.TestSecurityConfiguration.class})
class EngineControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void rejectsAnonymousEngineRequests() throws Exception {
        mockMvc.perform(get("/api/v1/engines")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void returnsRegisteredEngineMetadataAndHealth() throws Exception {
        mockMvc.perform(get("/api/v1/engines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("NATIVE"))
                .andExpect(jsonPath("$.data[0].deploymentMode").value("EMBEDDED"))
                .andExpect(jsonPath("$.data[0].capabilities").isArray())
                .andExpect(jsonPath("$.data[0].health.status").value("UP"));
    }

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        ExecutionEngine nativeEngine() {
            return new ExecutionEngine() {
                @Override public String engineType() { return "NATIVE"; }
                @Override public EngineMetadata metadata() { return EngineMetadata.test("NATIVE"); }
                @Override public EngineHealth healthCheck() {
                    return new EngineHealth(EngineHealthStatus.UP, "ok", Instant.EPOCH);
                }
            };
        }

        @Bean
        EngineRegistry engineRegistry(List<ExecutionEngine> engines) {
            return new EngineRegistry(engines);
        }

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
