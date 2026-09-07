package com.datacraft.api.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminUserRequest(
        @NotBlank(message = "用户名不能为空") @Size(max = 64, message = "用户名不能超过 64 个字符") String username,
        @NotBlank(message = "显示名不能为空") @Size(max = 128, message = "显示名不能超过 128 个字符") String displayName,
        @Size(max = 128, message = "密码不能超过 128 个字符") String password,
        List<String> roleCodes,
        Boolean enabled) {
}
