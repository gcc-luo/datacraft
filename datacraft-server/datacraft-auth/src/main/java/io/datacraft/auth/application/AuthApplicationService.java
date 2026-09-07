package io.datacraft.auth.application;

import io.datacraft.api.auth.LoginRequest;
import io.datacraft.api.auth.LoginResponse;
import io.datacraft.api.auth.UserSummary;
import io.datacraft.auth.domain.UserAccount;
import io.datacraft.auth.domain.UserRepository;
import io.datacraft.auth.security.DataCraftPrincipal;
import io.datacraft.auth.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public AuthApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount account = userRepository.findByUsername(request.username())
                .filter(UserAccount::canAuthenticate)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.passwordHash()))
                .orElseThrow(AuthenticationFailureException::new);
        return new LoginResponse(tokenService.issue(account), "Bearer", tokenService.expirationSeconds(), summary(account));
    }

    public UserSummary currentUser(DataCraftPrincipal principal) {
        return userRepository.findByUsername(principal.username()).map(this::summary)
                .orElseThrow(AuthenticationFailureException::new);
    }

    private UserSummary summary(UserAccount account) {
        return new UserSummary(account.id(), account.username(), account.displayName(), account.roleCodes());
    }
}
