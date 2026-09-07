package com.datacraft.api.system;

import java.time.Instant;

public record AdminMenuResponse(
        Long id,
        String code,
        String title,
        String path,
        String icon,
        Long parentId,
        String parentTitle,
        int sortOrder,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {
}
