package io.datacraft.auth.security;

import io.datacraft.auth.domain.UserAccount;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(
            "datacraft-phase-one-jwt-secret-32b".getBytes(StandardCharsets.UTF_8));

    @Test
    void signsAndParsesUserClaims() {
        JwtTokenService service = new JwtTokenService(new JwtProperties(SECRET, 3600));
        UserAccount account = new UserAccount(7L, "admin", "管理员", "hash", true, List.of("ADMIN"));

        String token = service.issue(account);
        DataCraftPrincipal principal = service.parse(token);

        assertThat(principal.userId()).isEqualTo(7L);
        assertThat(principal.username()).isEqualTo("admin");
        assertThat(principal.roles()).containsExactly("ADMIN");
    }

    @Test
    void rejectsTamperedAndExpiredTokens() {
        UserAccount account = new UserAccount(7L, "admin", "管理员", "hash", true, List.of("ADMIN"));
        JwtTokenService validService = new JwtTokenService(new JwtProperties(SECRET, 3600));

        assertThatThrownBy(() -> validService.parse(validService.issue(account) + "tampered"))
                .isInstanceOf(InvalidTokenException.class);

        JwtTokenService expiredService = new JwtTokenService(new JwtProperties(SECRET, 0));
        assertThatThrownBy(() -> expiredService.parse(expiredService.issue(account)))
                .isInstanceOf(InvalidTokenException.class);
    }
}
