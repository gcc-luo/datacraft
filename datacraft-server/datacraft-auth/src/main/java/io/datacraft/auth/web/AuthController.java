package io.datacraft.auth.web;

import io.datacraft.api.auth.LoginRequest;
import io.datacraft.api.auth.LoginResponse;
import io.datacraft.api.auth.UserSummary;
import io.datacraft.auth.application.AuthApplicationService;
import io.datacraft.auth.security.DataCraftPrincipal;
import io.datacraft.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserSummary> me(@AuthenticationPrincipal DataCraftPrincipal principal) {
        return ApiResponse.success(authService.currentUser(principal));
    }
}
