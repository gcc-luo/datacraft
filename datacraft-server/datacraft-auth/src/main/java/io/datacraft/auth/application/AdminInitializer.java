package io.datacraft.auth.application;

import io.datacraft.auth.domain.UserAccount;
import io.datacraft.auth.domain.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AdminInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            @Value("${datacraft.admin.username:}") String username,
                            @Value("${datacraft.admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("DATACRAFT_ADMIN_USERNAME and DATACRAFT_ADMIN_PASSWORD must be configured");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        UserAccount account = userRepository.insert(new UserAccount(null, username, username,
                passwordEncoder.encode(password), true, List.of("ADMIN")));
        userRepository.assignRole(account.id(), "ADMIN");
    }
}
