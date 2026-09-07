package com.datacraft.api.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminMenuRequest(
        @NotBlank(message = "菜单编码不能为空") @Size(max = 64, message = "菜单编码不能超过 64 个字符") String code,
        @NotBlank(message = "菜单标题不能为空") @Size(max = 128, message = "菜单标题不能超过 128 个字符") String title,
        @NotBlank(message = "菜单路径不能为空") @Size(max = 255, message = "菜单路径不能超过 255 个字符") String path,
        @Size(max = 64, message = "菜单图标不能超过 64 个字符") String icon,
        Long parentId,
        Integer sortOrder,
        Boolean enabled) {
}
