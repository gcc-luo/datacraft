package com.datacraft.api.system;

import java.util.List;

public record MenuDto(
        String code,
        String title,
        String path,
        String icon,
        Long parentId,
        int sortOrder,
        List<MenuDto> children) {
}
