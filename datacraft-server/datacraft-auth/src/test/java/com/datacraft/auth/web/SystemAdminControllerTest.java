package com.datacraft.auth.web;

import com.datacraft.api.system.AdminUserResponse;
import com.datacraft.auth.application.SystemAdminApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemAdminController.class)
@Import({SystemAdminController.class, ApiExceptionHandler.class, SystemAdminControllerTest.MethodSecurityTestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class SystemAdminControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean SystemAdminApplicationService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listsUsersForAdmin() throws Exception {
        when(service.listUsers()).thenReturn(List.of(new AdminUserResponse(1L, "admin", "管理员", true,
                List.of("ADMIN"), null, null)));

        mockMvc.perform(get("/api/v1/system/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[0].roleCodes[0]").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/system/admin/users"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }
}
