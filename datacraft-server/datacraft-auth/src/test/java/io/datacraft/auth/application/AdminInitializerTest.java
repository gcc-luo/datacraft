package io.datacraft.auth.application;

import io.datacraft.auth.domain.UserAccount;
import io.datacraft.auth.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AdminInitializerTest {
    @Test
    void createsAdminOnceAndDoesNotReplaceExistingPassword() {
        CapturingRepository repository = new CapturingRepository();
        AdminInitializer initializer = new AdminInitializer(repository, new BCryptPasswordEncoder(), "admin", "secret");

        initializer.initialize();
        initializer.initialize();

        assertThat(repository.insertCount).isEqualTo(1);
        assertThat(repository.saved.passwordHash()).startsWith("$2");
        assertThat(repository.saved.username()).isEqualTo("admin");
    }

    private static final class CapturingRepository implements UserRepository {
        private UserAccount saved;
        private int insertCount;
        @Override public Optional<UserAccount> findByUsername(String username) { return Optional.ofNullable(saved); }
        @Override public UserAccount insert(UserAccount account) { saved = new UserAccount(1L, account.username(), account.displayName(), account.passwordHash(), account.enabled(), account.roleCodes()); insertCount++; return saved; }
        @Override public void assignRole(Long userId, String roleCode) { }
    }
}
