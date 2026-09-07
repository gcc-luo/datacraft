package com.datacraft.api.system;

import java.time.Instant;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        String displayName,
        boolean enabled,
        List<String> roleCodes,
        Instant createdAt,
        Instant updatedAt) {
}
