package com.datacraft.api.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminRoleRequest(
        @NotBlank(message = "角色编码不能为空") @Size(max = 64, message = "角色编码不能超过 64 个字符") String code,
        @NotBlank(message = "角色名称不能为空") @Size(max = 128, message = "角色名称不能超过 128 个字符") String name,
        List<Long> menuIds,
        Boolean enabled) {
}
