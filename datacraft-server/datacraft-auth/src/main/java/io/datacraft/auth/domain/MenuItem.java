package io.datacraft.auth.domain;

public record MenuItem(
        Long id,
        String code,
        String title,
        String path,
        String icon,
        Long parentId,
        int sortOrder,
        boolean enabled) {
}
