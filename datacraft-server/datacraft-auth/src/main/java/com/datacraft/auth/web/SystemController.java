package com.datacraft.auth.web;

import com.datacraft.api.system.MenuDto;
import com.datacraft.auth.application.MenuApplicationService;
import com.datacraft.auth.security.DataCraftPrincipal;
import com.datacraft.common.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {
    private final MenuApplicationService menuService;

    public SystemController(MenuApplicationService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuDto>> menus(@AuthenticationPrincipal DataCraftPrincipal principal) {
        return ApiResponse.success(menuService.menusForRoles(principal.roles()));
    }
}
