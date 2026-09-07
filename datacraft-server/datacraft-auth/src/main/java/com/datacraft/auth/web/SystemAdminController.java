package com.datacraft.auth.web;

import com.datacraft.api.system.AdminMenuRequest;
import com.datacraft.api.system.AdminMenuResponse;
import com.datacraft.api.system.AdminRoleRequest;
import com.datacraft.api.system.AdminRoleResponse;
import com.datacraft.api.system.AdminUserRequest;
import com.datacraft.api.system.AdminUserResponse;
import com.datacraft.auth.application.SystemAdminApplicationService;
import com.datacraft.common.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/admin")
@PreAuthorize("hasRole('ADMIN')")
public class SystemAdminController {
    private final SystemAdminApplicationService service;

    public SystemAdminController(SystemAdminApplicationService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> users() {
        return ApiResponse.success(service.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<AdminUserResponse> createUser(@Valid @RequestBody AdminUserRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody AdminUserRequest request) {
        return ApiResponse.success(service.updateUser(id, request));
    }

    @GetMapping("/roles")
    public ApiResponse<List<AdminRoleResponse>> roles() {
        return ApiResponse.success(service.listRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<AdminRoleResponse> createRole(@Valid @RequestBody AdminRoleRequest request) {
        return ApiResponse.success(service.createRole(request));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<AdminRoleResponse> updateRole(@PathVariable Long id,
                                                      @Valid @RequestBody AdminRoleRequest request) {
        return ApiResponse.success(service.updateRole(id, request));
    }

    @GetMapping("/menus")
    public ApiResponse<List<AdminMenuResponse>> menus() {
        return ApiResponse.success(service.listMenus());
    }

    @PostMapping("/menus")
    public ApiResponse<AdminMenuResponse> createMenu(@Valid @RequestBody AdminMenuRequest request) {
        return ApiResponse.success(service.createMenu(request));
    }

    @PutMapping("/menus/{id}")
    public ApiResponse<AdminMenuResponse> updateMenu(@PathVariable Long id,
                                                      @Valid @RequestBody AdminMenuRequest request) {
        return ApiResponse.success(service.updateMenu(id, request));
    }
}
