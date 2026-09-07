package com.datacraft.quality.web;

import com.datacraft.api.quality.QualityResultResponse;
import com.datacraft.quality.application.QualityApplicationService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({QualityController.class, QualityExceptionHandler.class, QualityControllerTest.TestSecurityConfiguration.class})
class QualityControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean QualityApplicationService service;

    @Test
    void rejectsAnonymousQualityRequests() throws Exception {
        mockMvc.perform(post("/api/v1/quality/checks").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void returnsQualityResultsForAuthenticatedRequest() throws Exception {
        when(service.check(any())).thenReturn(List.of(new QualityResultResponse(7L, null, null, "NULL_CHECK", 1L,
                "customer", "phone", 10L, 1L, 9L, 0.9, "FAILED", List.of(), null)));

        mockMvc.perform(post("/api/v1/quality/checks").contentType("application/json")
                        .content("{\"datasourceId\":1,\"tableName\":\"customer\",\"rules\":[{\"type\":\"NULL_CHECK\",\"field\":\"phone\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalRows").value(10))
                .andExpect(jsonPath("$.data[0].passRate").value(0.9));
    }

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))).build();
        }
    }
}
