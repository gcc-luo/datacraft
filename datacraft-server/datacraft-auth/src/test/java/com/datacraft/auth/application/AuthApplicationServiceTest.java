package com.datacraft.auth.application;

import com.datacraft.api.auth.LoginRequest;
import com.datacraft.auth.domain.UserAccount;
import com.datacraft.auth.domain.UserRepository;
import com.datacraft.auth.security.JwtProperties;
import com.datacraft.auth.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthApplicationServiceTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(
            "datacraft-phase-one-jwt-secret-32b".getBytes(StandardCharsets.UTF_8));

    @Test
    void logsInWithValidPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserAccount account = new UserAccount(1L, "admin", "管理员", encoder.encode("correct"), true, List.of("ADMIN"));
        AuthApplicationService service = new AuthApplicationService(new SingleUserRepository(account), encoder,
                new JwtTokenService(new JwtProperties(SECRET, 3600)));

        var response = service.login(new LoginRequest("admin", "correct"));

        assertThat(response.user().username()).isEqualTo("admin");
        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    void rejectsWrongPasswordAndDisabledUser() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserAccount account = new UserAccount(1L, "admin", "管理员", encoder.encode("correct"), false, List.of("ADMIN"));
        AuthApplicationService service = new AuthApplicationService(new SingleUserRepository(account), encoder,
                new JwtTokenService(new JwtProperties(SECRET, 3600)));

        assertThatThrownBy(() -> service.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(AuthenticationFailureException.class);
        assertThatThrownBy(() -> service.login(new LoginRequest("admin", "correct")))
                .isInstanceOf(AuthenticationFailureException.class);
    }

    private record SingleUserRepository(UserAccount account) implements UserRepository {
        @Override public Optional<UserAccount> findByUsername(String username) {
            return account.username().equals(username) ? Optional.of(account) : Optional.empty();
        }
        @Override public UserAccount insert(UserAccount account) { return account; }
        @Override public void assignRole(Long userId, String roleCode) { }
    }
}
