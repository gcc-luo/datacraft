package com.datacraft.api.system;

import java.time.Instant;
import java.util.List;

public record AdminRoleResponse(
        Long id,
        String code,
        String name,
        boolean enabled,
        List<Long> menuIds,
        Instant createdAt,
        Instant updatedAt) {
}
