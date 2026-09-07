package io.datacraft.auth.security;

import io.datacraft.api.auth.UserSummary;
import io.datacraft.auth.application.AuthApplicationService;
import io.datacraft.auth.application.MenuApplicationService;
import io.datacraft.auth.domain.UserAccount;
import io.datacraft.auth.domain.UserRepository;
import io.datacraft.auth.web.AuthController;
import io.datacraft.auth.web.ApiExceptionHandler;
import io.datacraft.auth.web.SystemController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, SystemController.class})
@Import({SecurityConfig.class, AuthController.class, SystemController.class, ApiExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "datacraft.security.jwt.secret=ZGF0YWNyYWZ0LXBoYXNlLW9uZS1qd3Qtc2VjcmV0LTMyYg==",
        "datacraft.security.jwt.expiration-seconds=3600"
})
class SecurityConfigTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @Autowired UserDetailsService userDetailsService;
    @MockBean AuthApplicationService authService;
    @MockBean MenuApplicationService menuService;
    @MockBean UserRepository userRepository;

    @Test
    void rejectsAnonymousProtectedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/system/menus")).andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsValidBearerToken() throws Exception {
        UserAccount account = new UserAccount(1L, "admin", "管理员", "hash", true, List.of("ADMIN"));
        when(authService.currentUser(any())).thenReturn(new UserSummary(1L, "admin", "管理员", List.of("ADMIN")));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + tokenService.issue(account)))
                .andExpect(status().isOk());
    }

    @Test
    void usesRepositoryBackedUserDetailsService() {
        UserAccount account = new UserAccount(1L, "admin", "管理员", "hash", true, List.of("ADMIN"));
        when(userRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(account));

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        org.assertj.core.api.Assertions.assertThat(details.getUsername()).isEqualTo("admin");
        org.assertj.core.api.Assertions.assertThat(details.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }
}
